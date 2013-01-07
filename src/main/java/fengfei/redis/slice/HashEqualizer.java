package fengfei.redis.slice;

import redis.clients.util.Hashing;
import fengfei.redis.Plotter;

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
	public Slice get(String key) {
		int size = getSliceMap().size();
		long sk = Math.abs(hashed.hash(key) % size);
		return sliceMap.get(sk);
	}

}
