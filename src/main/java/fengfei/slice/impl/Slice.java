package fengfei.slice.impl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import fengfei.slice.SliceInfo;

public class Slice extends AbstractSlice {

	public Slice(SliceInfo master, SliceInfo[] slaves) {
		super(master, Arrays.asList(slaves));

	}

	public Slice(SliceInfo master, List<SliceInfo> slaves) {
		super(master, slaves == null ? null : slaves);
	}

	public static Slice createSlice(int timeout, String masterHost,
			String... slaveHosts) {
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
		return new Slice(master, slaves);

	}

}
