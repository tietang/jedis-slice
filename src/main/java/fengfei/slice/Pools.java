package fengfei.slice;

import java.util.List;
import java.util.NoSuchElementException;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;

import fengfei.slice.impl.Slice;

public interface Pools<T> {

	void closeAll();

	void close(ObjectPool<T> pool, SliceInfo info);

	void createPool(Equalizer equalizer);

	void createPool(Slice slice);

	void createPool(List<SliceInfo> sliceInfos);

	void createPool(SliceInfo sliceInfo);

	void remove(SliceInfo sliceInfo);

	T borrow(SliceInfo sliceInfo) throws NoSuchElementException,
			IllegalStateException, Exception;

	void returnPool(SliceInfo sliceInfo, T T) throws Exception;
	 PoolableObjectFactory<T> create(SliceInfo info);
}