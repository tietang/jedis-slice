package fengfei.redis.slice;

import java.util.Map;

import fengfei.redis.Plotter;

/**
 * key-> long % size->slice
 * 
 * 
 */
public class LongModuleEqualizer extends AbstractEqualizer {

	public LongModuleEqualizer() {
	}

	public LongModuleEqualizer(Plotter plotter, Map<Long, Slice> sliceMap) {
		super(plotter);
		this.sliceMap = sliceMap;
	}

	public LongModuleEqualizer(Plotter plotter) {
		super(plotter);

	}

	@Override
	public Slice get(String key) {
		int size = getSliceMap().size();
		long mod = Long.parseLong(key);
		long sk = Math.abs(mod % size);
		return sliceMap.get(sk);
	}

}
