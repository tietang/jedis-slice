package fengfei.shard.redis;

import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.GenericObjectPool.Config;

import redis.clients.jedis.Jedis;
import fengfei.shard.InstanceInfo;
import fengfei.shard.impl.AbstractPools;

public class JedisPools extends AbstractPools<Jedis> {

	public JedisPools(Config config) {
		super(config);
	}

	@Override
	public PoolableObjectFactory<Jedis> create(InstanceInfo info) {
		return new PoolableRedisFactory(info.getHost(), info.getPort(),
				info.getTimeout(), info.getPassword());
	}

    @Override
    public String toString() {
        return "JedisPools [poolMap=" + poolMap + ", config=" + config + "]";
    }

}
