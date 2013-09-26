package fengfei.shard;

import fengfei.shard.exception.ShardException;

import java.util.List;


public interface ShardSelector {

    Shard select(String key, List<Shard> shards) throws ShardException;

}
