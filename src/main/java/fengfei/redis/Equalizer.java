package fengfei.redis;

import java.util.Map;

import fengfei.redis.slice.Slice;

public interface Equalizer {

	/**
	 * 
	 * @param key
	 * @param size
	 *            slice size
	 * @return
	 */
	SliceInfo get(byte[] key, int readWrite) throws Exception;

	SliceInfo get(String key, int readWrite) throws Exception;

	/**
	 * add ext-map
	 * 
	 * @param redisSliceMap
	 */
	void mapSlice(Map<Long, Slice> redisSliceMap);

	/**
	 * add slice
	 * 
	 * @param id
	 *            slice id
	 * @param master
	 *            host:port
	 * @param slaves
	 *            [host1:port, host2:port]
	 */
	void addSlice(Long id, Slice slice);

	/**
	 * default:
	 * 
	 * <pre>
	 * HashPlotter
	 * </pre>
	 * 
	 * @param plotter
	 */
	void setPlotter(Plotter plotter);

	Plotter getPlotter();

	Map<Long, Slice> getSliceMap();
}
