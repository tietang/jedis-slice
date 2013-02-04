package fengfei.redis.slice;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.impl.GenericObjectPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;
import fengfei.redis.Equalizer;
import fengfei.redis.Plotter;
import fengfei.redis.RedisComand;
import fengfei.redis.SliceInfo;

/**
 * <pre>
 * example 1:
 * 		PoolableSlicedRedis redis = new PoolableSlicedRedis(
 * 				"192.168.1.3:6379,192.168.1.4:6379,192.168.1.5:6379 192.168.1.6:6379,192.168.1.7:6379,192.168.1.8:6379", 60000, new HashEqualizer(),
 * 				config);
 * 		RedisComand rc = redis.createRedisCommand();
 * 	    rc.set("key", "value");
 * 		redis.close();
 * 
 * * example 2: 
 * 		Equalizer equalizer = new HashEqualizer();
 * 		equalizer.setTimeout(60);
 * 		equalizer.setPoolConfig(config);
 * 		equalizer.setPlotter(new LoopPlotter());
 * 		//slice 0: master:192.168.1.3:6379 slave:192.168.1.4:6379 192.168.1.5:6379
 * 		equalizer.addSlice(0, "192.168.1.3:6379", "192.168.1.4:6379",
 * 				"192.168.1.5:6379");
 * 		//slice 1: master:192.168.1.6:6379 slave:192.168.1.7:6379 192.168.1.8:6379
 * 		equalizer.addSlice(0, "192.168.1.6:6379", "192.168.1.7:6379",
 * 				"192.168.1.8:6379");
 * 
 * 		PoolableSlicedRedis redis = new PoolableSlicedRedis(equalizer);
 * 		RedisComand rc = redis.createRedisCommand();
 * </pre>
 * 
 * @author
 * 
 */
public class SlicedRedis {
	final static int ReadWrite = 0;
	final static int ReadOnly = 2;
	final static int WriteOnly = 1;

	private static Logger logger = LoggerFactory.getLogger(SlicedRedis.class);
	protected Map<SliceInfo, ObjectPool<Jedis>> poolMap = new ConcurrentHashMap<>();
	private Equalizer equalizer = new HashEqualizer();
	private AtomicLong lastId = new AtomicLong();
	private boolean isPoolable = false;

	private static GenericObjectPool.Config defaultConfig;
	static {
		defaultConfig = new GenericObjectPool.Config();
		defaultConfig.maxActive = 10;
		defaultConfig.maxIdle = 10;
		defaultConfig.minIdle = 2;
		defaultConfig.maxWait = 60000;
		defaultConfig.testOnBorrow = true;
		defaultConfig.whenExhaustedAction = GenericObjectPool.WHEN_EXHAUSTED_FAIL;
	}

	public SlicedRedis(Equalizer equalizer, boolean isPoolable) {
		this.equalizer = equalizer;
		this.isPoolable = isPoolable;
		createPool(equalizer, defaultConfig);
	}

	public SlicedRedis(Equalizer equalizer, GenericObjectPool.Config config) {
		this.equalizer = equalizer;
		this.isPoolable = true;
		createPool(equalizer, config);

	}

	public SlicedRedis(String hosts, int timeout, Equalizer equalizer,
			boolean isPoolable) {
		super();
		this.isPoolable = isPoolable;
		init(hosts, timeout, equalizer.getPlotter(), null);
	}

	public SlicedRedis(String hosts, int timeout, Equalizer equalizer,
			GenericObjectPool.Config config) {
		super();
		this.isPoolable = true;
		init(hosts, timeout, equalizer.getPlotter(), config);
	}

	public SlicedRedis(String hosts, int timeout, Plotter plotter,
			boolean isPoolable) {
		super();
		this.isPoolable = isPoolable;
		init(hosts, timeout, plotter, null);
	}

	public SlicedRedis(String hosts, int timeout, Plotter plotter,
			GenericObjectPool.Config config) {
		super();
		this.isPoolable = true;
		init(hosts, timeout, plotter, config);
	}

	private void init(String hosts, int timeout, Plotter plotter,
			GenericObjectPool.Config config) {
		String[] allhosts = hosts.split(" ");

		for (int j = 0; j < allhosts.length; j++) {
			String mshosts = allhosts[j];
			String sliceHosts[] = mshosts.split(",");
			String masterHost = sliceHosts[0];
			String slaveHosts[] = null;
			if (sliceHosts.length > 1) {
				slaveHosts = new String[sliceHosts.length - 1];
				List<String> sliceSlaves = new ArrayList<>();
				for (int i = 1; i < sliceHosts.length; i++) {
					sliceSlaves.add(sliceHosts[i]);
				}
				slaveHosts = sliceSlaves.toArray(slaveHosts);
			}
			long id = lastId.get();
			Slice slice = Slice.createSlice(timeout, masterHost, slaveHosts);
			equalizer.addSlice(id, slice);
			createPool(slice, config);
			lastId.incrementAndGet();
		}

	}

	private void createPool(Slice slice, GenericObjectPool.Config config) {
		List<SliceInfo> master = new ArrayList<>();
		master.add(slice.master);
		createPool(config, master);
		createPool(config, slice.slaves);
	}

	private void createPool(Equalizer equalizer, GenericObjectPool.Config config) {
		if (isPoolable) {
			Set<Entry<Long, Slice>> sets = equalizer.getSliceMap().entrySet();
			for (Entry<Long, Slice> entry : sets) {
				Slice slice = entry.getValue();
				List<SliceInfo> master = new ArrayList<>();
				master.add(slice.master);
				createPool(config, master);
				createPool(config, slice.slaves);
			}
		}
	}

	private void createPool(GenericObjectPool.Config config,
			List<SliceInfo> sliceInfos) {
		if (isPoolable) {

			if (config == null) {
				config = defaultConfig;
			}
			for (SliceInfo info : sliceInfos) {
				ObjectPool<Jedis> pool = poolMap.get(info);
				if (pool == null) {
					pool = new GenericObjectPool<>(new PoolableRedisFactory(
							info.getHost(), info.getPort(),
							info.getTimeout() * 1000), config);
					poolMap.put(info, pool);
					logger.debug("created pool for: " + info);
				} else {
					logger.debug("pool isn't created, already exists pool for: "
							+ info);
				}

			}

		}
	}

	/**
	 * <pre>
	 * eg: 
	 *  	masterHost : 192.168.1.22:6300
	 *  	slaveHosts : 192.168.1.22:6301,192.168.1.22:6302,192.168.1.22:6303
	 * 	addHosts("192.168.1.22:6300", "192.168.1.22:6301", "192.168.1.22:6302", "192.168.1.22:6303")
	 * </pre>
	 * 
	 * @param masterHost
	 * @param slaveHosts
	 */
	public void addHosts(int timeout, String masterHost, String... slaveHosts) {
		long id = lastId.get();
		Slice slice = Slice.createSlice(timeout, masterHost, slaveHosts);
		equalizer.addSlice(id, slice);
		lastId.incrementAndGet();
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

	public void close() {
		Set<Entry<SliceInfo, ObjectPool<Jedis>>> sets = poolMap.entrySet();
		for (Entry<SliceInfo, ObjectPool<Jedis>> entry : sets) {
			SliceInfo info = entry.getKey();
			ObjectPool<Jedis> pool = entry.getValue();
			try {
				pool.clear();
			} catch (Throwable e) {

				logger.error("clear pool error for:" + info.toString(), e);
			}
			try {
				pool.close();
			} catch (Throwable e) {
				logger.error("close pool error for: " + info.toString(), e);
			}

		}

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
			ObjectPool<Jedis> pool = null;

			Jedis jedis = null;
			SliceInfo sliceInfo = null;
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
				sliceInfo = equalizer.get(new String(key), readWrite);

				pool = poolMap.get(sliceInfo);
				jedis = pool.borrowObject();
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
				pool.returnObject(jedis);
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
			SliceInfo sliceInfo = null;
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
				sliceInfo = equalizer.get(new String(key), readWrite);

				jedis = jedisConnect(sliceInfo);
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

	private static Jedis jedisConnect(SliceInfo sliceInfo)
			throws JedisConnectionException {
		try {
			Jedis jedis = new Jedis(sliceInfo.getHost(), sliceInfo.getPort(),
					sliceInfo.getTimeout());

			jedis.connect();
			if (null != sliceInfo.getPassword()) {
				jedis.auth(sliceInfo.getPassword());
			}
			return jedis;
		} catch (Exception e) {
			throw new JedisConnectionException("Can't connect host:"
					+ sliceInfo.getHost() + ":" + sliceInfo.getPort(), e);
		}

	}

	public static interface RetryCallback {

		void execute() throws Exception;
	}

}
