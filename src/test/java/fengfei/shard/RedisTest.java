package fengfei.shard;

import fengfei.shard.impl.HashSelector;
import fengfei.shard.impl.Shards;
import fengfei.shard.redis.PoolableRedisFactoryCreator;
import fengfei.shard.redis.RedisCommand;
import org.junit.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import redis.clients.jedis.Jedis;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@Ignore
public class RedisTest {
    private static Logger logger = LoggerFactory.getLogger(RedisTest.class);

    static Shards<Jedis> shards;

    static int size = 60;

    @BeforeClass
    public static void setup() {

        shards = new Shards<>("127.0.0.1:6379 127.0.0.1:6379", 60000,
                new HashSelector(), new PoolableRedisFactoryCreator(), true);
//		shards = new JedisShards("127.0.0.1:6379 127.0.0.1:6379", 60000,
//				new HashSelector(), true);
        // System.out.println(router);
    }

    public static void main(String[] args) {
        shards = new Shards<Jedis>("127.0.0.1:6379 127.0.0.1:6379", 60000,
                new HashSelector(), new PoolableRedisFactoryCreator(), true);
        RedisCommand cmd = shards.create(RedisCommand.class);

        for (; ; ) {
            try {
                Thread.sleep(1000);
                String pong = cmd.ping();
                System.out.println(pong);

            } catch (Exception e) {
                logger.error("XXX: ", e);
            }
        }


    }

    @AfterClass
    public static void unsetup() {

        shards.close();
    }

    @Test
    public void testPing() {

        RedisCommand cmd = shards.create(RedisCommand.class);
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

        RedisCommand rc = shards.create(RedisCommand.class);
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


}
