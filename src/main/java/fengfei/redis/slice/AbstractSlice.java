package fengfei.redis.slice;

import fengfei.redis.Plotter;

public abstract class AbstractSlice {

	public final static int StatusNormal = 1;
	public final static int StatusError = 0;

	protected SliceInfo master;

	protected SliceInfo[] slaves;

	protected int slaveSize;
	protected Plotter plotter;

	protected int status = StatusNormal;

	public AbstractSlice(SliceInfo master, SliceInfo[] slaves, Plotter plotter) {
		super();
		this.master = master;
		this.slaves = slaves;
		this.slaveSize = slaves == null ? 0 : slaves.length;
		this.plotter = plotter;

	}

 

	public abstract SliceInfo getMaster(byte[] key);

	public abstract SliceInfo getAny(byte[] key);

	public abstract SliceInfo getNextSlave(byte[] key);

	public int getStatus() {
		return status;
	}

}
