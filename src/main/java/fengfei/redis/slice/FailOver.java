package fengfei.redis.slice;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool.PoolableObjectFactory;

import redis.clients.jedis.Jedis;
import fengfei.redis.Equalizer;
import fengfei.redis.SliceInfo;

public class FailOver implements Runnable {
	ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(
			2);
	Equalizer equalizer;

	Pools pools;

	public FailOver(Equalizer equalizer, Pools pools) {
		super();
		this.equalizer = equalizer;
		this.pools = pools;
	}

	public void start() {
		scheduledExecutorService.scheduleAtFixedRate(this, 1, 1,
				TimeUnit.MINUTES);
	}

	public void exit() {
		scheduledExecutorService.shutdown();
	}

	@Override
	public void run() {
		Map<Long, Slice> ms = equalizer.getSliceMap();
		Set<Long> keys = ms.keySet();
		for (Long key : keys) {
			Slice slice = ms.get(key);
			List<SliceInfo> infos = slice.configSlaves;
			for (SliceInfo sliceInfo : infos) {
				boolean isConnected = test(sliceInfo);
				if (isConnected) {
					slice.recoverSlave(sliceInfo);
					pools.createPool(sliceInfo);
				} else {
					slice.cancelSlave(sliceInfo);
					pools.remove(sliceInfo);
				}
			}

		}

	}

	private boolean test(SliceInfo sliceInfo) {
		Jedis jedis = null;
		PoolableObjectFactory<Jedis> poolableObjectFactory = null;
		try {
			poolableObjectFactory = new PoolableRedisFactory(
					sliceInfo.getHost(), sliceInfo.getPort(),
					sliceInfo.getTimeout() * 1000, sliceInfo.getPassword());
			jedis = poolableObjectFactory.makeObject();
			return poolableObjectFactory.validateObject(jedis);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		} finally {
			try {
				poolableObjectFactory.destroyObject(jedis);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
}
