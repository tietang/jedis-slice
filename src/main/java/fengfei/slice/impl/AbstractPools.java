package fengfei.slice.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fengfei.slice.Equalizer;
import fengfei.slice.Pools;
import fengfei.slice.SliceInfo;

public abstract class AbstractPools<T> implements Pools<T> {
	private static Logger logger = LoggerFactory.getLogger(AbstractPools.class);
	protected Map<SliceInfo, ObjectPool<T>> poolMap = new ConcurrentHashMap<>();

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

	public AbstractPools(Config config) {
		super();

		this.config = config;
	}

	public void closeAll() {
		Set<Entry<SliceInfo, ObjectPool<T>>> sets = poolMap.entrySet();
		for (Entry<SliceInfo, ObjectPool<T>> entry : sets) {
			SliceInfo info = entry.getKey();
			ObjectPool<T> pool = entry.getValue();
			close(pool, info);
		}

	}

	public void close(ObjectPool<T> pool, SliceInfo info) {
		try {
			pool.clear();
		} catch (Throwable e) {
			logger.error("clear pool error for:" + info.toString(), e);
		}
		try {
			pool.close();
		} catch (Throwable e) {
			logger.error("close pool error for: " + info.toString(), e);
		}
	}

	public void createPool(Equalizer equalizer) {

		Set<Entry<Long, Slice>> sets = equalizer.getSliceMap().entrySet();
		for (Entry<Long, Slice> entry : sets) {
			Slice slice = entry.getValue();
			createPool(slice);
		}

	}

	public void createPool(Slice slice) {
		List<SliceInfo> master = new ArrayList<>();
		master.add(slice.getMaster());
		createPool(master);
		createPool(slice.getSlaves());
	}

	public void createPool(List<SliceInfo> sliceInfos) {

		if (config == null) {
			config = DefaultConfig;
		}
		for (SliceInfo info : sliceInfos) {
			createPool(info);
		}

	}

	public abstract PoolableObjectFactory<T> create(SliceInfo info);

	public void createPool(SliceInfo sliceInfo) {

		ObjectPool<T> pool = poolMap.get(sliceInfo);
		if (pool == null) {
			pool = new GenericObjectPool<>(create(sliceInfo), config);
			poolMap.put(sliceInfo, pool);
			logger.debug("created pool for: " + sliceInfo);
		} else {
			logger.debug("pool isn't created, already exists pool for: "
					+ sliceInfo);
		}

	}

	public void remove(SliceInfo sliceInfo) {
		ObjectPool<T> pool = poolMap.remove(sliceInfo);
		close(pool, sliceInfo);
	}

	public T borrow(SliceInfo sliceInfo) throws NoSuchElementException,
			IllegalStateException, Exception {
		ObjectPool<T> pool = poolMap.get(sliceInfo);
		return pool.borrowObject();
	}

	public void returnPool(SliceInfo sliceInfo, T T) throws Exception {
		ObjectPool<T> pool = poolMap.get(sliceInfo);
		pool.returnObject(T);
	}
}
