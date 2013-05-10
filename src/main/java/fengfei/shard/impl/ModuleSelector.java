package fengfei.shard.impl;

import fengfei.shard.Ploy;
import fengfei.shard.Shard;

public class ModuleSelector extends HashSelector {

    public ModuleSelector() {
        super();
    }

    public ModuleSelector(Ploy ploy) {
        super();
        this.ploy = ploy;
    }

    public int calculate(String key, int shardSize) {
        long lkey = Long.parseLong(key);
        return (int) Math.abs(lkey % shardSize);
    }

    public Shard selectShard(int key) {
        return shards.get(key);
    }

}
