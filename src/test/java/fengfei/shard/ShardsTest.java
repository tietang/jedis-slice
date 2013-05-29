package fengfei.shard;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.apache.commons.pool.PoolableObjectFactory;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import fengfei.shard.ServerHelper.Clientx;
import fengfei.shard.ServerHelper.RPCInterface;
import fengfei.shard.ServerHelper.Serverx;
import fengfei.shard.impl.Shards;
import fengfei.shard.impl.HashSelector;
import fengfei.shard.impl.PoolableObjectFactoryCreator;

public class ShardsTest {

	static Shards<Clientx> shard;
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
		shard = new Shards<>("127.0.0.1:1980 127.0.0.1:1980 ", 60000,
				new HashSelector(), new TestPoolableFactoryCreator(), true);
		// System.out.println(router);
	}

	@AfterClass
	public static void unsetup() {
		serverx.close();
		shard.close();
	}

	@Test
	public void testPing() {

		RPCInterface iface = shard.create(RPCInterface.class);
		try {
			for (int i = 0; i < 100; i++) {
				String pong = iface.ping();
				assertEquals("pong", pong);
			}

		} catch (Exception e) {
			assertTrue(false);
		}
	}

	@Test
	public void testHello() {

		RPCInterface iface = shard.create(RPCInterface.class);
		try {
			for (int j = 0; j < 10; j++) {
				String name = "hello" + j;
				String rv = iface.hello(name);
				Assert.assertNotNull(rv);
				assertEquals("hello, " + name, rv);

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
