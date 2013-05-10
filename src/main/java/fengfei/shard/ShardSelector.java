package fengfei.shard;

import java.util.List;


public interface ShardSelector {

    Shard select(String key, List<Shard> shards) throws Exception;

}
