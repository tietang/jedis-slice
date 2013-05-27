package fengfei.shard.impl;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import fengfei.shard.InstanceInfo;
import fengfei.shard.Ploy;
import fengfei.shard.Selector;
import fengfei.shard.Shard;
import fengfei.shard.Status;
import fengfei.shard.exception.ShardException;

public abstract class AbstractPloy implements Ploy {
	@Override
	public InstanceInfo select(String key, Shard shard, int readWrite)
			throws ShardException {
		InstanceInfo info = null;
		switch (readWrite) {
		case Selector.ReadWrite:
			info = getAny(shard, key);
			break;
		case Selector.Read:
			info = getNextSlave(shard, key);
			break;
		case Selector.Write:
			info = getMaster(shard, key);
			break;

		default:
			break;
		}
		if (info == null) {
			throw new ShardException("Can't find instance for key: " + key);
		}

		if (info.getStatus() != Status.Normal) {
			throw new ShardException(
					"Current instance is unavailable, cause is "
							+ info.getStatus().name() + ", "
							+ info.toInfoString());
		}
		return info;
	}

	public InstanceInfo getMaster(Shard shard, String key)
			throws ShardException {
		return shard.getMaster();
	}

	public InstanceInfo getAny(Shard shard, String key) throws ShardException {
		List<InstanceInfo> slaves = shard.getSlaves();
		InstanceInfo master = shard.getMaster();

		List<InstanceInfo> all = new ArrayList<>(slaves);
		all.add(master);
		InstanceInfo info = get(all, new HashSet<InstanceInfo>(), shard, key);
		return info;
	}

	protected InstanceInfo get(List<InstanceInfo> all,
			HashSet<InstanceInfo> failSlaves, Shard shard, String key)
			throws ShardException {

		HashSet<InstanceInfo> allSlaves = new HashSet<>(all);

		if (failSlaves.size() == allSlaves.size()
				&& failSlaves.equals(allSlaves)) {
			throw new ShardException(
					"Current shard has not available instance for key: " + key);
		}

		InstanceInfo master = shard.getMaster();
		int index = calculate(key, allSlaves.size());
		InstanceInfo info = (all == null) ? master : all.get(index);
		if (info.getStatus() != Status.Normal) {
			failSlaves.add(info);
			info = get(all, failSlaves, shard, key);
		}
		return info;
	}

	public InstanceInfo getNextSlave(Shard shard, String key)
			throws ShardException {
		List<InstanceInfo> slaves = shard.getSlaves();
		List<InstanceInfo> all = new ArrayList<>(slaves);
		InstanceInfo info = get(all, new HashSet<InstanceInfo>(), shard, key);
		return info;
	}

	public abstract int calculate(String key, int size);
}
