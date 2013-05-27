package fengfei.shard.redis;

import org.apache.commons.pool.impl.GenericObjectPool;

import redis.clients.jedis.Jedis;
import fengfei.shard.Ploy;
import fengfei.shard.Selector;
import fengfei.shard.impl.Shards;

public class JedisShards extends Shards<Jedis> {

	public JedisShards(Selector selector, boolean isPoolable) {
		super(selector, new PoolableRedisFactoryCreator(), isPoolable);
	}

	public JedisShards(Selector selector, GenericObjectPool.Config config) {
		super(selector, new PoolableRedisFactoryCreator(), config);
	}

	/**
	 * <pre>
	 * 
	 *  hosts: MasterHost:port[,SlaveHost:port...] MasterHost:port[,SlaveHost:port...] ...
	 * </pre>
	 * 
	 * @param hosts
	 * @param timeout
	 * @param selector
	 * @param isPoolable
	 */
	public JedisShards(String hosts, int timeout, Selector selector,
			boolean isPoolable) {
		super(hosts, timeout, selector, new PoolableRedisFactoryCreator(),
				isPoolable);
	}

	public JedisShards(String hosts, int timeout, Selector selector,
			GenericObjectPool.Config config) {
		super(hosts, timeout, selector, new PoolableRedisFactoryCreator(),
				config);
	}

	public JedisShards(String hosts, int timeout, Ploy ploy, boolean isPoolable) {
		super(hosts, timeout, ploy, new PoolableRedisFactoryCreator(),
				isPoolable);
	}

	public JedisShards(String hosts, int timeout, Ploy ploy,
			GenericObjectPool.Config config) {
		super(hosts, timeout, ploy, new PoolableRedisFactoryCreator(), config);
	}

}
