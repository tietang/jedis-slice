package fengfei.shard.redis;

import org.apache.commons.pool.PoolableObjectFactory;

import redis.clients.jedis.Jedis;
import fengfei.shard.InstanceInfo;
import fengfei.shard.impl.PoolableObjectFactoryCreator;

public class PoolableRedisFactoryCreator implements
		PoolableObjectFactoryCreator<Jedis> {

	@Override
	public PoolableObjectFactory<Jedis> create(InstanceInfo info) {
		return new PoolableRedisFactory(info.getHost(), info.getPort(),
				info.getTimeout(), info.getPassword());
	}

}
