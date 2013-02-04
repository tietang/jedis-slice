package fengfei.redis.slice;

import redis.clients.util.Hashing;
import fengfei.redis.Plotter;
import fengfei.redis.SliceInfo;

/**
 * key -> hash % size->slice
 * 
 * 
 */
public class HashEqualizer extends AbstractEqualizer {

	protected Hashing hashed = Hashing.MD5;

	public HashEqualizer() {

	}

	public HashEqualizer(Plotter plotter) {
		super(plotter);
	}

	@Override
	public SliceInfo get(byte[] key, int readWrite) throws Exception {
		int size = getSliceMap().size();
		long sk = Math.abs(hashed.hash(key) % size);
		Slice slice = sliceMap.get(sk);
		if (slice == null) {
			throw new Exception("can't find slice.");
		}
		return getPlotter().get(key, slice, readWrite);
	}

}
