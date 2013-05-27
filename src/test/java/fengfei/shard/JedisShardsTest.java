package fengfei.shard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.pool.PoolableObjectFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;

import redis.clients.jedis.Jedis;
import fengfei.shard.ServerHelper.Clientx;
import fengfei.shard.ServerHelper.Serverx;
import fengfei.shard.impl.HashSelector;
import fengfei.shard.impl.PoolableObjectFactoryCreator;
import fengfei.shard.impl.Shards;
import fengfei.shard.redis.JedisShards;
import fengfei.shard.redis.PoolableRedisFactoryCreator;
import fengfei.shard.redis.RedisComand;

public class JedisShardsTest {

	static Shards<Jedis> shards;
	static Serverx serverx = new Serverx();
	static int size = 60;

	@BeforeClass
	public static void setup() {
		Thread t = new Thread() {

			public void run() {
				try {
					serverx.start();
				} catch (Exception e) {
					e.printStackTrace();
				}
			};
		};
		t.start();
		shards = new Shards<>("127.0.0.1:6379 127.0.0.1:6379", 60000,
				new HashSelector(), new PoolableRedisFactoryCreator(), true);
//		shards = new JedisShards("127.0.0.1:6379 127.0.0.1:6379", 60000,
//				new HashSelector(), true);
		// System.out.println(router);
	}

	@AfterClass
	public static void unsetup() {
		serverx.close();
		shards.close();
	}

	@Test
	public void testPing() {

		RedisComand cmd = shards.create(RedisComand.class);
		try {
			for (int i = 0; i < 100; i++) {
				String pong = cmd.ping();
				assertEquals("PONG", pong);
			}

		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testSet() {

		RedisComand rc = shards.create(RedisComand.class);
		try {
			for (int i = 0; i < 10; i++) {
				String key = "K" + i;
				String value = "V" + i;
				rc.set(key, value);
				String v = rc.get(key);
				Assert.assertNotNull(v);
				assertEquals(value, v);
			}

		} catch (Exception e) {
			assertTrue(false);
		}
	}

	static class TestPoolableFactoryCreator implements
			PoolableObjectFactoryCreator<Clientx> {

		@Override
		public PoolableObjectFactory<Clientx> create(InstanceInfo info) {
			return new TestPoolableObjectFactory(info);
		}

	}

	static class TestPoolableObjectFactory implements
			PoolableObjectFactory<Clientx> {
		InstanceInfo info;

		public TestPoolableObjectFactory(InstanceInfo info) {
			this.info = info;
		}

		@Override
		public Clientx makeObject() throws Exception {
			return new Clientx(info.host, info.port);
		}

		@Override
		public void destroyObject(Clientx obj) throws Exception {
			obj.close();
		}

		@Override
		public boolean validateObject(Clientx obj) {
			try {
				return "pong".equals(obj.ping());
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}

		@Override
		public void activateObject(Clientx obj) throws Exception {
		}

		@Override
		public void passivateObject(Clientx obj) throws Exception {
		}
	};
}
