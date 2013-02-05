package fengfei.slice.impl;

import redis.clients.util.Hashing;
import fengfei.slice.Plotter;

public class HashPlotter extends AbstractPlotter implements Plotter {
	protected Hashing hashed = Hashing.MURMUR_HASH;

	@Override
	public int get(byte[] key, int size) {
		return Math.abs((Long.valueOf(hashed.hash(key) % size).intValue()));
	}

}
