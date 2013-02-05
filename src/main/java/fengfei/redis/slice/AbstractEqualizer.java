package fengfei.redis.slice;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import fengfei.redis.Equalizer;
import fengfei.redis.Plotter;
import fengfei.redis.SliceInfo;

/**
 * key -> hash % size->slice
 * 
 * 
 */
public abstract class AbstractEqualizer implements Equalizer {
	protected Map<Long, Slice> sliceMap = new ConcurrentHashMap<>();
	protected Plotter plotter;

	public AbstractEqualizer() {
		plotter = new HashPlotter();
	}

	public AbstractEqualizer(Plotter plotter) {
		super();
		this.plotter = plotter;
	}

	@Override
	public void mapSlice(Map<Long, Slice> redisSliceMap) {
		sliceMap = new ConcurrentHashMap<>(redisSliceMap);
	}

	@Override
	public void addSlice(Long id, Slice slice) {
		getSliceMap().put(id, slice);
	}

	@Override
	public void setPlotter(Plotter plotter) {
		this.plotter = plotter;
	}

	@Override
	public Map<Long, Slice> getSliceMap() {
		return sliceMap;
	}

	@Override
	public Plotter getPlotter() {

		return plotter;
	}

	@Override
	public SliceInfo get(String key, int readWrite) throws Exception {
		return get(key.getBytes(), readWrite);
	}
}
