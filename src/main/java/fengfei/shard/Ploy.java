package fengfei.shard;


public interface Ploy {

    InstanceInfo select(String key, Shard shard, int readWrite);
}
