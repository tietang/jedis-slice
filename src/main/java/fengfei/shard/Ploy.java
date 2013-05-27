package fengfei.shard;

import fengfei.shard.exception.ShardException;

public interface Ploy {

	InstanceInfo select(String key, Shard shard, int readWrite)
			throws ShardException;
}
