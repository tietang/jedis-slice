package fengfei.redis.example;

import fengfei.shard.impl.HashSelector;
import fengfei.shard.redis.JedisShards;
import fengfei.shard.redis.RedisCommand;
import redis.clients.jedis.Pipeline;

public class PipelineExample {

    private static boolean isPoolable = false;

    /**
     * @param args
     */
    public static void main(String[] args) {
        isPoolable = true;

        example1();

    }

    public static void example1() {
        JedisShards redis = new JedisShards(
                "172.17.20.73:6380 172.17.20.73:6380",
                60000,
                new HashSelector(),
                isPoolable);
        RedisCommand rc = redis.create(RedisCommand.class);
        rc.flushDB();

        Pipeline p = rc.pipelined();
        for (int i = 0; i < 10; i++) {
            p.set("K" + i, "V" + i);
        }
        p.sync();
        for (int i = 0; i < 10; i++) {
            String v = rc.get("K" + i);
            System.out.println(v);
        }
        redis.close();
    }

}
