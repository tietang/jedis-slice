package fengfei.shard.impl;

import java.util.List;

import fengfei.shard.InstanceInfo;
import fengfei.shard.Ploy;
import fengfei.shard.Selector;
import fengfei.shard.Shard;

public abstract class AbstractPloy implements Ploy {
    @Override
    public InstanceInfo select(String key, Shard shard, int readWrite) {
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
        return info;
    }

    public InstanceInfo getMaster(Shard shard, String key) {
        return shard.getMaster();
    }

    public InstanceInfo getAny(Shard shard, String key) {
        List<InstanceInfo> slaves = shard.getSlaves();
        InstanceInfo master = shard.getMaster();
        int index = calculate(key, slaves.size() + 1);
        return (slaves == null || index == slaves.size()) ? master : slaves.get(index);
    }

    public InstanceInfo getNextSlave(Shard shard, String key) {
        List<InstanceInfo> slaves = shard.getSlaves();
        InstanceInfo master = shard.getMaster();
        if (slaves == null || slaves.size() == 0) {
            return master;
        }
        return slaves.get(calculate(key, slaves.size()));
    }

    public abstract int calculate(String key, int size);
}
