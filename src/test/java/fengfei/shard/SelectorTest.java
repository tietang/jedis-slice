package fengfei.shard;

import static fengfei.shard.Selector.Read;
import static fengfei.shard.Selector.Write;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.*;

import java.util.List;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.Test;

import fengfei.shard.impl.HashSelector;

public class SelectorTest {

	Selector selector = null;

	@Before
	public void setUpBeforeClass() throws Exception {
		selector = new HashSelector();
		int timeout = 30;

		selector.addShard(Shard.createShard(timeout, "192.168.1.11:8002",
				"192.168.1.12:8002", "192.168.1.13:8002"));
		selector.addShard(Shard.createShard(timeout, "192.168.1.21:8002",
				"192.168.1.22:8002", "192.168.1.23:8002"));

	}

	@AfterClass
	public static void tearDownAfterClass() throws Exception {
	}

	@Test
	public void testSelect() {
		for (int i = 0; i < 10; i++) {
			try {
				InstanceInfo info = selector.select("key1" + i, Write);
				System.out.println(info);
				assertNotNull(info);
				assertTrue(info.isMaster());
			} catch (Exception e) {
				e.printStackTrace();
				assertTrue(false);
			}
			try {
				InstanceInfo info = selector.select("key1" + i, Read);
				System.out.println(info);
				assertNotNull(info);
				assertTrue(!info.isMaster());
			} catch (Exception e) {
				e.printStackTrace();
				assertTrue(false);
			}
		}

	}

	@Test
	public void testSelectMaster() {
		for (int i = 0; i < 10; i++) {
			try {
				InstanceInfo info = selector.selectMaster("master" + i);
				System.out.println(info);
				assertNotNull(info);
				assertTrue(info.isMaster());
			} catch (Exception e) {
				e.printStackTrace();
				assertTrue(false);
			}

		}
	}



	@Test
	public void testSelectSlave() {
		for (int i = 0; i < 10; i++) {
			try {
				InstanceInfo info = selector.selectSlave("slave" + i);
				System.out.println(info);
				assertNotNull(info);
				assertTrue(!info.isMaster());
			} catch (Exception e) {
				e.printStackTrace();
				assertTrue(false);
			}

		}
	}

	@Test
	public void testSelectAny() {
		for (int i = 0; i < 10; i++) {
			try {
				InstanceInfo info = selector.selectAny("any" + i);
				;
				System.out.println(info);
				assertNotNull(info);
			} catch (Exception e) {
				e.printStackTrace();
				assertTrue(false);
			}

		}
	}

}
