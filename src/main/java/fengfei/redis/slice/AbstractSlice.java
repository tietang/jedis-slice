package fengfei.redis.slice;

import java.util.ArrayList;
import java.util.List;

import fengfei.redis.SliceInfo;

public abstract class AbstractSlice {

	public final static int StatusNormal = 1;
	public final static int StatusError = 0;

	protected SliceInfo master;
	protected List<SliceInfo> slaves;
	protected List<SliceInfo> cancelledSlaves = new ArrayList<>();
	protected List<SliceInfo> configSlaves;

	protected int status = StatusNormal;

	public AbstractSlice(SliceInfo master, List<SliceInfo> slaves) {
		super();
		this.master = master;
		this.slaves = slaves;

		configSlaves = this.slaves;
		if (slaves == null) {
			slaves = new ArrayList<>();
		}
	}

	public int getStatus() {
		return status;
	}

	public void cancelSlave(SliceInfo sliceInfo) {
		cancelledSlaves.add(sliceInfo);
		slaves.remove(sliceInfo);
	}

	public void recoverSlave(SliceInfo sliceInfo) {
		slaves.add(sliceInfo);
		cancelledSlaves.remove(sliceInfo);
	}

	public List<SliceInfo> getConfigSlaves() {
		return configSlaves;
	}

	public List<SliceInfo> getCancelledSlaves() {
		return cancelledSlaves;
	}

}
