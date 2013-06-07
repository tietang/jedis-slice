package fengfei.shard.impl;

import java.util.Map.Entry;
import java.util.TreeMap;

import fengfei.shard.InstanceInfo;
import fengfei.shard.Ploy;
import fengfei.shard.Shard;
import fengfei.shard.exception.ShardRuntimeExcption;

public class HashModSelector extends HashSelector {

    private int maxSize = 1024;
    private TreeMap<Integer, Integer> mod2Shard = new TreeMap<>();

    public HashModSelector() {
        super();
    }

    public HashModSelector(int maxSize) {
        super();
        this.maxSize = maxSize;
    }

    public HashModSelector(Ploy ploy) {
        super(ploy);
    }

    public HashModSelector(Ploy ploy, int maxSize) {
        super(ploy);
        this.maxSize = maxSize;
    }

    public void setMaxModSize(int maxSize) {
        this.maxSize = maxSize;
    }

    protected void reAssign() {
        mod2Shard.clear();
        int size = maxSize / shards.size();
        for (int i = 1; i <= shards.size(); i++) {
            int base = i * size;
            if (i == shards.size()) {
                base = maxSize;
            }
            mod2Shard.put(base, i - 1);
        }
        // System.out.println(mod2Shard);
    }

    public void addShard(Shard shard) {
        super.addShard(shard);
        reAssign();
    }

    public void addShard(int index, Shard shard) {
        super.addShard(index, shard);
        reAssign();
    }

    public void addInstanceInfo(int index, InstanceInfo info) {
        super.addInstanceInfo(index, info);
        reAssign();
    }

    public int calculate(String key, int shardSize) {
        int index = (int) Math.abs(hashed.hash(key) % maxSize);
        Entry<Integer, Integer> entry = mod2Shard.ceilingEntry(index);

        if (entry == null) {
            throw new ShardRuntimeExcption("the given key(" + key + ") less than or equal to  "
                    + maxSize);
        } else {
            Integer idx = entry.getValue();
            return idx.intValue();
        }

    }

}
