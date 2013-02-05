package fengfei.slice.impl;

import java.util.List;

import fengfei.slice.Plotter;
import fengfei.slice.SliceInfo;

public abstract class AbstractPlotter implements Plotter {

	@Override
	public SliceInfo get(byte[] key, AbstractSlice slice, int readWrite) {
		SliceInfo sliceInfo = null;
		switch (readWrite) {
		case ReadWrite:
			sliceInfo = getAny(slice, key);
			break;
		case ReadOnly:
			sliceInfo = getNextSlave(slice, key);
			break;
		case WriteOnly:
			sliceInfo = getMaster(slice, key);
			break;

		default:
			break;
		}
		return sliceInfo;
	}

	public SliceInfo getMaster(AbstractSlice slice, byte[] key) {
		return slice.master;
	}

	public SliceInfo getAny(AbstractSlice slice, byte[] key) {
		List<SliceInfo> slaves = slice.slaves;
		SliceInfo master = slice.master;
		int index = get(key, slaves.size() + 1);
		return (slaves == null || index == slaves.size()) ? master : slaves
				.get(index);
	}

	public SliceInfo getNextSlave(AbstractSlice slice, byte[] key) {
		List<SliceInfo> slaves = slice.slaves;
		SliceInfo master = slice.master;
		if (slaves == null || slaves.size() == 0) {
			return master;
		}
		return slaves.get(get(key, slaves.size()));
	}
}
