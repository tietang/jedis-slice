package fengfei.shard;

import java.util.List;

public interface Selector {

    public final static int ReadWrite = 0;
    public final static int Write = 1;
    public final static int Read = 2;

    InstanceInfo select(String key, int readWrite) throws Exception;

    InstanceInfo selectMaster(String key) throws Exception;

    InstanceInfo selectSlave(String key) throws Exception;

    InstanceInfo selectAny(String key) throws Exception;

    void addShard(Shard shard);

    void addShard(int index, Shard shard);

    void addInstanceInfo(int index, InstanceInfo info);

    List<Shard> getShards();

    Ploy getPloy();

    void setPloy(Ploy ploy);
}
