package fengfei.redis.slice;

import java.util.ArrayList;
import java.util.List;

import fengfei.redis.Plotter;

public class Slice extends AbstractSlice  {

	public Slice(SliceInfo master, SliceInfo[] slaves, Plotter plotter) {
		super(master, slaves, plotter);

	}

	public Slice(SliceInfo master, List<SliceInfo> slaves, Plotter plotter) {
		this(master, slaves == null ? null : (slaves
				.toArray(new SliceInfo[slaves.size()])), plotter);
	}

	public static Slice createSlice(int timeout, Plotter plotter,
			String masterHost, String... slaveHosts) {
		String mhp[] = masterHost.split(":");
		SliceInfo master = new SliceInfo(mhp[0], Integer.parseInt(mhp[1]),
				timeout);
		List<SliceInfo> slaves = new ArrayList<>();
		if (slaveHosts != null && slaveHosts.length > 0) {

			for (String shost : slaveHosts) {
				String shp[] = shost.split(":");
				SliceInfo slave = new SliceInfo(shp[0],
						Integer.parseInt(shp[1]), timeout);
				slaves.add(slave);
			}

		}
		return new Slice(master, slaves, plotter);

	}

	public SliceInfo getMaster(byte[] key) {
		return master;
	}

	public SliceInfo getAny(byte[] key) {
		int index = plotter.get(key, slaveSize + 1);
		return (slaves == null || index == slaves.length) ? master
				: slaves[index];
	}

	public SliceInfo getNextSlave(byte[] key) {
		if (slaves == null || slaves.length == 0) {
			return master;
		}
		return slaves[plotter.get(key, slaveSize)];
	}

}
