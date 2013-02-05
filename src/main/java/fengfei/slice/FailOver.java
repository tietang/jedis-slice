package fengfei.slice;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.pool.PoolableObjectFactory;

import fengfei.slice.impl.Slice;

public class FailOver<T> implements Runnable {
	ScheduledExecutorService scheduledExecutorService = new ScheduledThreadPoolExecutor(
			2);
	Equalizer equalizer;

	Pools<T> pools;

	public FailOver(Equalizer equalizer, Pools<T> pools) {
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
			List<SliceInfo> infos = slice.getConfigSlaves();
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
		T t = null;
		PoolableObjectFactory<T> poolableObjectFactory = pools
				.create(sliceInfo);
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
