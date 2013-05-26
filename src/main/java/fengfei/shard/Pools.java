package fengfei.shard;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.pool.ObjectPool;

import fengfei.shard.impl.PoolableObjectFactoryCreator;

public interface Pools<T> {

	void closeAll();

	void close(ObjectPool<T> pool, InstanceInfo info);

	void createPool(Selector selector);

	void createPool(Shard shard);

	void createPool(List<InstanceInfo> infos);

	void createPool(InstanceInfo info);

	void remove(InstanceInfo info);

	T borrow(InstanceInfo info) throws NoSuchElementException,
			IllegalStateException, Exception;

	void returnPool(InstanceInfo info, T T) throws Exception;

	public PoolableObjectFactoryCreator<T> getPoolableObjectFactoryCreator();
}