package fengfei.slice.redis;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

import redis.clients.jedis.Jedis;
import fengfei.slice.SliceInfo;
import fengfei.slice.impl.AbstractPools;

public class JedisPools extends AbstractPools<Jedis> {

	public JedisPools(Config config) {
		super(config);
	}

	@Override
	public PoolableObjectFactory<Jedis> create(SliceInfo info) {
		return new PoolableRedisFactory(info.getHost(), info.getPort(),
				info.getTimeout(), info.getPassword());
	}

}
