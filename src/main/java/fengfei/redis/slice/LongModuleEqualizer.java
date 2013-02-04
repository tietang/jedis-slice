package fengfei.redis.slice;

import java.util.Map;

import fengfei.redis.Plotter;
import fengfei.redis.SliceInfo;

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
	public SliceInfo get(byte[] key, int readWrite) throws Exception {
		return get(new String(key), readWrite);
	}

	@Override
	public SliceInfo get(String key, int readWrite) throws Exception {
		int size = getSliceMap().size();
		long mod = Long.parseLong(key);
		long sk = Math.abs(mod % size);
		Slice slice = sliceMap.get(sk);
		if (slice == null) {
			throw new Exception("can't find slice.");
		}
		return getPlotter().get(key.getBytes(), slice, readWrite);
	}

}
