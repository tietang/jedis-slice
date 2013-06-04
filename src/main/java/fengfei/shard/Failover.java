package fengfei.shard;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.pool.PoolableObjectFactory;

public class Failover<T> implements Runnable {

    final static int MaxRetryTimes = 10;
    final static int IntervalSecond = 60;

    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2);
    Selector selector;

    Pools<T> pools;
    Map<InstanceInfo, CheckCounter> checkCounters = new HashMap<>();

    public Failover(Selector selector, Pools<T> pools) {
        super();
        this.selector = selector;
        this.pools = pools;
    }

    public void start() {
        scheduledExecutorService.scheduleAtFixedRate(this, 1, 1, TimeUnit.MINUTES);
    }

    public void exit() {
        scheduledExecutorService.shutdown();
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
