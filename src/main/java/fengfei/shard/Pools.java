package fengfei.shard;

import fengfei.shard.impl.PoolableObjectFactoryCreator;
import org.apache.commons.pool.ObjectPool;

import java.util.List;

public interface Pools<T> {

    void closeAll();

    void close(ObjectPool<T> pool, InstanceInfo info);

    void createPool(Selector selector);

    void createPool(Shard shard);

    void createPool(List<InstanceInfo> infos);

    void createPool(InstanceInfo info);

    void remove(InstanceInfo info);

    T borrow(InstanceInfo info) throws Exception;

    void returnPool(InstanceInfo info, T T) throws Exception;

    public PoolableObjectFactoryCreator<T> getPoolableObjectFactoryCreator();
}