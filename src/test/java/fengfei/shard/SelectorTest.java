package fengfei.shard;

import static org.junit.Assert.*;
import static fengfei.shard.Selector.*;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import fengfei.shard.impl.HashSelector;

public class SelectorTest {

    static Selector selector = new HashSelector();

    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
        int timeout = 30;

        selector.addShard(Shard.createShard(
            timeout,
            "192.168.1.11:8002",
            "192.168.1.12:8002",
            "192.168.1.13:8002"));
        selector.addShard(Shard.createShard(
            timeout,
            "192.168.1.21:8002",
            "192.168.1.22:8002",
            "192.168.1.23:8002"));

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
                InstanceInfo info = selector.selectAny("any"+i);
;                System.out.println(info);
                assertNotNull(info);
            } catch (Exception e) {
                e.printStackTrace();
                assertTrue(false);
            }

        }
    }

}
