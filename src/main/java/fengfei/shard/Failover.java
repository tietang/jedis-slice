package fengfei.shard;

import java.util.List;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool.PoolableObjectFactory;

public class Failover<T> implements Runnable {

    ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(2);
    Selector selector;

    Pools<T> pools;

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
                boolean isConnected = test(info);
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

    private boolean test(InstanceInfo info) {
        T t = null;
        PoolableObjectFactory<T> poolableObjectFactory = pools.create(info);
        try {
            t = poolableObjectFactory.makeObject();
            return poolableObjectFactory.validateObject(t);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        } finally {
            try {
                poolableObjectFactory.destroyObject(t);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}