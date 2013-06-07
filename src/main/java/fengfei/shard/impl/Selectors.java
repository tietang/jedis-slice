package fengfei.shard.impl;

import java.util.ArrayList;
import java.util.List;

import fengfei.shard.Ploy;
import fengfei.shard.Selector;
import fengfei.shard.Shard;

public class Selectors {

    public static Selector newHashSelector(String hosts, int timeout, Ploy ploy) {
        Selector selector = new HashSelector();
        initSelector(selector, hosts, timeout, ploy);
        return selector;
    }

    public static Selector newHashSelector(String hosts, int timeout) {
        Selector selector = new HashSelector();
        initSelector(selector, hosts, timeout);
        return selector;
    }

    public static Selector newHashModSelector(String hosts, int timeout, Ploy ploy) {
        Selector selector = new HashModSelector();
        initSelector(selector, hosts, timeout, ploy);
        return selector;
    }

    public static Selector newHashModSelector(String hosts, int timeout) {
        Selector selector = new HashModSelector();
        initSelector(selector, hosts, timeout);
        return selector;
    }

    public static Selector newModuleSelector(String hosts, int timeout, Ploy ploy) {
        Selector selector = new ModuleSelector();
        initSelector(selector, hosts, timeout, ploy);
        return selector;
    }

    public static Selector newModuleSelector(String hosts, int timeout) {
        Selector selector = new ModuleSelector();
        initSelector(selector, hosts, timeout);
        return selector;
    }

    public static void initSelector(Selector selector, String hosts, int timeout) {
        initSelector(selector, hosts, timeout, null);
    }

    public static void initSelector(Selector selector, String hosts, int timeout, Ploy ploy) {
        if (ploy != null) {
            selector.setPloy(ploy);
        }
        String[] allhosts = hosts.split(" ");
        int index = 0;
        for (int j = 0; j < allhosts.length; j++) {
            String mshosts = allhosts[j];
            String shardHosts[] = mshosts.split(",");
            String masterHost = shardHosts[0];
            String slaveHosts[] = null;
            if (shardHosts.length > 1) {
                slaveHosts = new String[shardHosts.length - 1];
                List<String> ShardSlaves = new ArrayList<>();
                for (int i = 1; i < shardHosts.length; i++) {
                    ShardSlaves.add(shardHosts[i]);
                }
                slaveHosts = ShardSlaves.toArray(slaveHosts);
            }

            Shard shard = Shard.createShard(index++, timeout, masterHost, slaveHosts);
            selector.addShard(shard);

        }
    }
}
