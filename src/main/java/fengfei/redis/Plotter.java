package fengfei.redis;

import fengfei.redis.slice.AbstractSlice;

public interface Plotter {
	public final static int ReadWrite = 0;
	public final static int ReadOnly = 2;
	public final static int WriteOnly = 1;

	/**
	 * 
	 * @param key
	 * 
	 * @param size
	 *            all instances's size in slice
	 * @return
	 */
	int get(byte[] key, int size);

	SliceInfo get(byte[] key, AbstractSlice slice, int readWrite);

}
