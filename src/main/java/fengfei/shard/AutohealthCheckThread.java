package fengfei.shard;

import org.apache.commons.pool.PoolableObjectFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class AutoHealthCheckThread<T> implements Runnable {
    private static Logger logger = LoggerFactory.getLogger(AutoHealthCheckThread.class);
    final static int MaxRetryTimes = 10;
    final static int IntervalSecond = 60;
    private Object lockObject = new Object();
    private CountDownLatch startSignal = new CountDownLatch(1);
    Set<InstanceInfo> immediatelyChecked = new HashSet<>();
    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2);
    Selector selector;

    Pools<T> pools;
    Map<InstanceInfo, CheckCounter> checkCounters = new HashMap<>();

    public AutoHealthCheckThread(Selector selector, Pools<T> pools) {
        super();
        this.selector = selector;
        this.pools = pools;
    }

    public void start() {

        scheduledExecutorService.scheduleAtFixedRate(this, 1, 1, TimeUnit.MINUTES);
        new Thread() {
            @Override
            public void run() {
                while (true) {
                    if (immediatelyChecked.size() == 0) try {
                        startSignal.await();
//                        lockObject.wait();
                    } catch (InterruptedException e) {
                        logger.error("", e);
                    }
                    for (InstanceInfo instanceInfo : immediatelyChecked) {
                        try {
                            boolean isConnected = test(instanceInfo);
                            if (isConnected) {
                                pools.createPool(instanceInfo);
                                immediatelyChecked.remove(instanceInfo);
                            } else {
                                pools.remove(instanceInfo);
                            }
                        } catch (Exception e) {
                            logger.error("", e);
                        }
                    }
                }

            }
        }.start();
    }

    public void exit() {
        scheduledExecutorService.shutdown();
    }

    public void add(InstanceInfo instanceInfo) {
        immediatelyChecked.add(instanceInfo);
//        startSignal.countDown();
        lockObject.notifyAll();
    }

    @Override
    public void run() {
        List<Shard> shards = selector.getShards();
        for (Shard shard : shards) {
            List<InstanceInfo> infos = shard.getConfigedInfos();
            for (InstanceInfo info : infos) {
                CheckCounter counter = getCheckCounter(info);
                if ((counter.lastTime - System.currentTimeMillis()) >= IntervalSecond * 1000
                        && counter.counter.get() >= MaxRetryTimes) {

                } else {
                    boolean isConnected = test(info);
                    counter.lastTime = System.currentTimeMillis();
                    counter.counter.getAndIncrement();
                    if (isConnected) {
                        shard.recover(info);
                        pools.createPool(info);
                    } else {
                        shard.cancel(info);
                        pools.remove(info);
                        logger.warn("The server has lost, to remove it from pool:" + info);
                    }
                }
            }
        }

    }

    Lock lock = new ReentrantLock();

    private CheckCounter getCheckCounter(InstanceInfo info) {
        lock.lock();
        try {
            CheckCounter counter = checkCounters.get(info);
            if (counter == null) {
                counter = new CheckCounter();
                checkCounters.put(info, counter);
            }
            return counter;
        } finally {
            lock.unlock();
        }

    }

    private boolean test(InstanceInfo info) {
        T t = null;
        PoolableObjectFactory<T> poolableObjectFactory = pools
                .getPoolableObjectFactoryCreator()
                .create(info);
        try {
            t = poolableObjectFactory.makeObject();
            return poolableObjectFactory.validateObject(t);
        } catch (Exception e) {
            // e.printStackTrace();
            return false;
        } finally {
            try {
                poolableObjectFactory.destroyObject(t);
            } catch (Exception e) {
                // e.printStackTrace();
            }
        }
    }


    private static class CheckCounter {

        public long lastTime = 0;
        public AtomicInteger counter = new AtomicInteger(0);

        public int incr() {
            return counter.incrementAndGet();
        }

    }
}
