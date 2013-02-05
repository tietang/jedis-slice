package fengfei.redis.slice;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool.Config;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import fengfei.redis.Equalizer;
import fengfei.redis.SliceInfo;

public class Pools {
	private static Logger logger = LoggerFactory.getLogger(Pools.class);
	protected Map<SliceInfo, ObjectPool<Jedis>> poolMap = new ConcurrentHashMap<>();

	private GenericObjectPool.Config config;
	private static GenericObjectPool.Config defaultConfig;
	static {
		defaultConfig = new GenericObjectPool.Config();
		defaultConfig.maxActive = 10;
		defaultConfig.maxIdle = 10;
		defaultConfig.minIdle = 2;
		defaultConfig.maxWait = 60000;
		defaultConfig.testOnBorrow = true;
		defaultConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
	}

	public Pools(Config config) {
		super();

		this.config = config;
	}

	public void closeAll() {
		Set<Entry<SliceInfo, ObjectPool<Jedis>>> sets = poolMap.entrySet();
		for (Entry<SliceInfo, ObjectPool<Jedis>> entry : sets) {
			SliceInfo info = entry.getKey();
			ObjectPool<Jedis> pool = entry.getValue();
			close(pool, info);
		}

	}

	public void close(ObjectPool<Jedis> pool, SliceInfo info) {
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

	public void createPool(Slice slice) {
		List<SliceInfo> master = new ArrayList<>();
		master.add(slice.master);
		createPool(master);
		createPool(slice.slaves);
	}

	public void createPool(Equalizer equalizer) {

		Set<Entry<Long, Slice>> sets = equalizer.getSliceMap().entrySet();
		for (Entry<Long, Slice> entry : sets) {
			Slice slice = entry.getValue();
			List<SliceInfo> master = new ArrayList<>();
			master.add(slice.master);
			createPool(master);
			createPool(slice.slaves);
		}

	}

	public void createPool(List<SliceInfo> sliceInfos) {

		if (config == null) {
			config = defaultConfig;
		}
		for (SliceInfo info : sliceInfos) {
			ObjectPool<Jedis> pool = poolMap.get(info);
			if (pool == null) {
				pool = new GenericObjectPool<>(new PoolableRedisFactory(
						info.getHost(), info.getPort(),
						info.getTimeout() * 1000, info.getPassword()), config);
				poolMap.put(info, pool);
				logger.debug("created pool for: " + info);
			} else {
				logger.debug("pool isn't created, already exists pool for: "
						+ info);
			}

		}

	}

	public void createPool(SliceInfo sliceInfo) {

		ObjectPool<Jedis> pool = poolMap.get(sliceInfo);
		if (pool == null) {
			pool = new GenericObjectPool<>(new PoolableRedisFactory(
					sliceInfo.getHost(), sliceInfo.getPort(),
					sliceInfo.getTimeout() * 1000, sliceInfo.getPassword()),
					config);
			poolMap.put(sliceInfo, pool);
			logger.debug("created pool for: " + sliceInfo);
		} else {
			logger.debug("pool isn't created, already exists pool for: "
					+ sliceInfo);
		}

	}

	public void remove(SliceInfo sliceInfo) {
		ObjectPool<Jedis> pool = poolMap.remove(sliceInfo);
		close(pool, sliceInfo);
	}

	public Jedis borrowJedis(SliceInfo sliceInfo)
			throws NoSuchElementException, IllegalStateException, Exception {
		ObjectPool<Jedis> pool = poolMap.get(sliceInfo);
		return pool.borrowObject();
	}

	public void returnJedis(SliceInfo sliceInfo, Jedis jedis) throws Exception {
		ObjectPool<Jedis> pool = poolMap.get(sliceInfo);
		pool.returnObject(jedis);
	}
}
