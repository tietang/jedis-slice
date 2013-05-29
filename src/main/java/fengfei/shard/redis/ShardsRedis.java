package fengfei.shard.redis;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import fengfei.shard.Failover;
import fengfei.shard.InstanceInfo;
import fengfei.shard.Ploy;
import fengfei.shard.Pools;
import fengfei.shard.Selector;
import fengfei.shard.Shard;
import fengfei.shard.impl.DefaultPools;
import fengfei.shard.impl.HashSelector;
import fengfei.shard.impl.Shards;

/**
 * <pre>
 * example 1:
 * 		PoolableSharddRedis redis = new PoolableSharddRedis(
 * 				"192.168.1.3:6379,192.168.1.4:6379,192.168.1.5:6379 192.168.1.6:6379,192.168.1.7:6379,192.168.1.8:6379", 60000, new Hashselector(),
 * 				config);
 * 		RedisComand rc = redis.createRedisCommand();
 * 	    rc.set("key", "value");
 * 		redis.close();
 * 
 * * example 2: 
 * 		Selector selector = new Hashselector();
 * 		selector.setTimeout(60);
 * 		selector.setPoolConfig(config);
 * 		selector.setploy(new Loopploy());
 * 		//Shard 0: master:192.168.1.3:6379 slave:192.168.1.4:6379 192.168.1.5:6379
 * 		selector.addShard(0, "192.168.1.3:6379", "192.168.1.4:6379",
 * 				"192.168.1.5:6379");
 * 		//Shard 1: master:192.168.1.6:6379 slave:192.168.1.7:6379 192.168.1.8:6379
 * 		selector.addShard(0, "192.168.1.6:6379", "192.168.1.7:6379",
 * 				"192.168.1.8:6379");
 * 
 * 		PoolableSharddRedis redis = new PoolableSharddRedis(selector);
 * 		RedisComand rc = redis.createRedisCommand();
 * </pre>
 * 
 * @author
 * 
 */
public class ShardsRedis extends Shards<Jedis> {

	private static Logger logger = LoggerFactory.getLogger(ShardsRedis.class);

	public ShardsRedis(Selector selector, boolean isPoolable) {
		super(selector, new PoolableRedisFactoryCreator(), isPoolable);
	}

	public ShardsRedis(Selector selector, GenericObjectPool.Config config) {
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
	public ShardsRedis(String hosts, int timeout, Selector selector,
			boolean isPoolable) {
		super(hosts, timeout, selector, new PoolableRedisFactoryCreator(),
				isPoolable);
	}

	public ShardsRedis(String hosts, int timeout, Selector selector,
			GenericObjectPool.Config config) {
		super(hosts, timeout, selector, new PoolableRedisFactoryCreator(),
				config);
	}

	public ShardsRedis(String hosts, int timeout, Ploy ploy, boolean isPoolable) {
		super(hosts, timeout, ploy, new PoolableRedisFactoryCreator(),
				isPoolable);
	}

	public ShardsRedis(String hosts, int timeout, Ploy ploy,
			GenericObjectPool.Config config) {
		super(hosts, timeout, ploy, new PoolableRedisFactoryCreator(), config);
	}

	public RedisComand createRedisCommand() {
		return createRedisCommand(ReadWrite);
	}

	public RedisComand createRedisCommand(int rw) {
		Class<RedisComand> iface = RedisComand.class;
		InvocationHandler handler = null;
		if (isPoolable) {
			handler = new PoolableRedisComandsHandler(rw);
			logger.debug("create RedisComand with pool.");
		} else {
			handler = new RedisComandsHandler(rw);
			logger.debug("create RedisComand without pool.");
		}
		return (RedisComand) Proxy.newProxyInstance(iface.getClassLoader(),
				new Class[] { iface }, handler);
	}

	Random random = new Random(19800202);

	private class PoolableRedisComandsHandler implements InvocationHandler {

		int readWrite = ReadWrite;

		public PoolableRedisComandsHandler(int readWrite) {
			super();
			this.readWrite = readWrite;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {

			Jedis jedis = null;
			InstanceInfo info = null;
			String key = null;
			try {

				Class<?> argsClass[] = method.getParameterTypes();
				if (args != null && args.length > 0) {
					// argsClass = new Class<?>[args.length];
					// for (int i = 0; i < args.length; i++) {
					// argsClass[i] = args[i].getClass();
					// System.out.println(argsClass[i].isPrimitive());
					// }

					Object obj = args[0];
					if (obj instanceof byte[]) {
						key = String.valueOf((byte[]) obj);
					} else {
						key = obj.toString();
					}
				} else {
					// argsClass = new Class<?>[] {};
					key = String.valueOf(random.nextInt());
				}
				info = selector.select(new String(key), readWrite);

				jedis = pools.borrow(info);
				if (jedis == null) {
					throw new Exception("can't connected redis for key:" + key);
				}
				if (!jedis.isConnected()) {
					throw new Exception("redis can't be connected for key:"
							+ key);
				}

				Method origin = Jedis.class.getMethod(method.getName(),
						argsClass);
				Object obj = origin.invoke(jedis, args);
				return obj;
			} catch (Throwable e) {
				logger.error("Can not operate redis for key:" + key, e);
				throw e;

			} finally {
				pools.returnPool(info, jedis);
			}
		}
	}

	private class RedisComandsHandler implements InvocationHandler {

		int readWrite = ReadWrite;

		public RedisComandsHandler(int readWrite) {
			super();
			this.readWrite = readWrite;
		}

		@Override
		public Object invoke(Object proxy, Method method, Object[] args)
				throws Throwable {

			Jedis jedis = null;
			InstanceInfo ShardInfo = null;
			try {
				byte[] key = null;

				Class<?> argsClass[] = method.getParameterTypes();
				if (args != null && args.length > 0) {
					// argsClass = new Class<?>[args.length];
					// for (int i = 0; i < args.length; i++) {
					// argsClass[i] = args[i].getClass();
					// System.out.println(argsClass[i].isPrimitive());
					// }

					Object obj = args[0];
					if (obj instanceof byte[]) {
						key = (byte[]) obj;
					} else {
						key = obj.toString().getBytes();
					}
				} else {
					// argsClass = new Class<?>[] {};
					key = String.valueOf(random.nextLong()).getBytes();
				}
				ShardInfo = selector.select(new String(key), readWrite);

				jedis = jedisConnect(ShardInfo);
				if (jedis == null) {
					throw new Exception("can't connected redis");
				}
				if (!jedis.isConnected()) {
					throw new Exception("redis can't be connected.");
				}
				Method origin = Jedis.class.getMethod(method.getName(),
						argsClass);
				Object obj = origin.invoke(jedis, args);
				return obj;
			} catch (Throwable e) {
				logger.error("Can not operate redis ", e);
				throw e;

			} finally {
				try {
					try {
						jedis.quit();
					} catch (Exception e) {
					}
					jedis.disconnect();
				} catch (Exception e) {
					logger.error("close jedis error ", e);
				}
			}
		}
	}

	private static Jedis jedisConnect(InstanceInfo ShardInfo)
			throws JedisConnectionException {
		try {
			Jedis jedis = new Jedis(ShardInfo.getHost(), ShardInfo.getPort(),
					ShardInfo.getTimeout());

			jedis.connect();
			if (null != ShardInfo.getPassword()) {
				jedis.auth(ShardInfo.getPassword());
			}
			return jedis;
		} catch (Exception e) {
			throw new JedisConnectionException("Can't connect host:"
					+ ShardInfo.getHost() + ":" + ShardInfo.getPort(), e);
		}

	}

	public static interface RetryCallback {

		void execute() throws Exception;
	}

}
