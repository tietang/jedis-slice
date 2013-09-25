package fengfei.shard.impl;

import fengfei.shard.InstanceInfo;
import fengfei.shard.Pools;
import fengfei.shard.Selector;
import fengfei.shard.Shard;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultPools<T> implements Pools<T> {

	private static Logger logger = LoggerFactory.getLogger(DefaultPools.class);
	protected Map<InstanceInfo, ObjectPool<T>> poolMap = new ConcurrentHashMap<>();
	protected PoolableObjectFactoryCreator<T> factoryCreator;
	protected GenericObjectPool.Config config;
	public static GenericObjectPool.Config DefaultConfig;

	static {
		DefaultConfig = new GenericObjectPool.Config();
		DefaultConfig.maxActive = 10;
		DefaultConfig.maxIdle = 10;
		DefaultConfig.minIdle = 2;
		DefaultConfig.maxWait = 60000;
		DefaultConfig.testOnBorrow = true;
		DefaultConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
	}

	public DefaultPools(Config config,
			PoolableObjectFactoryCreator<T> factoryCreator) {
		super();

		this.config = config;
		this.factoryCreator = factoryCreator;
	}

	public DefaultPools(PoolableObjectFactoryCreator<T> factoryCreator) {
		super();
		this.config = DefaultConfig;
		this.factoryCreator = factoryCreator;
	}

	public void closeAll() {
		Set<Entry<InstanceInfo, ObjectPool<T>>> sets = poolMap.entrySet();
		for (Entry<InstanceInfo, ObjectPool<T>> entry : sets) {
			InstanceInfo info = entry.getKey();
			ObjectPool<T> pool = entry.getValue();
			close(pool, info);
		}

	}

	public PoolableObjectFactoryCreator<T> getPoolableObjectFactoryCreator() {
		return factoryCreator;
	}

	public void close(ObjectPool<T> pool, InstanceInfo info) {
		if (pool == null) {
			return;
		}
		try {
			pool.close();
			pool.clear();
			pool = null;

		} catch (Throwable e) {
			logger.error("clear pool error for:" + info.toString(), e);
		}

	}

	public void createPool(Selector selector) {
		List<Shard> shards = selector.getShards();
		for (Shard shard : shards) {
			createPool(shard);
		}

	}

	public void createPool(Shard shard) {
		List<InstanceInfo> master = new ArrayList<>();
		master.add(shard.getMaster());
		createPool(master);
		createPool(shard.getSlaves());
	}

	public void createPool(List<InstanceInfo> infos) {

		if (config == null) {
			config = DefaultConfig;
		}
		for (InstanceInfo info : infos) {
			createPool(info);
		}

	}

	public void createPool(InstanceInfo info) {

		ObjectPool<T> pool = poolMap.get(info);
		if (pool == null) {
			pool = new GenericObjectPool<>(factoryCreator.create(info), config);
			poolMap.put(info, pool);
			logger.debug("created pool for host: " + info);
		} else {
			logger.debug("pool isn't created, already exists pool for: " + info);
		}

	}

	public void remove(InstanceInfo info) {
		ObjectPool<T> pool = poolMap.remove(info);
		close(pool, info);
	}

	public T borrow(InstanceInfo info) throws Exception {
		ObjectPool<T> pool = poolMap.get(info);
		if (pool == null)
			return null;
		return pool.borrowObject();
	}

	public void returnPool(InstanceInfo info, T T) throws Exception {
		ObjectPool<T> pool = poolMap.get(info);

		if (pool != null) {
			pool.returnObject(T);
		}
	}
}
