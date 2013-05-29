package fengfei.redis.example;

import redis.clients.jedis.Pipeline;
import fengfei.shard.impl.HashSelector;
import fengfei.shard.redis.RedisComand;
import fengfei.shard.redis.ShardsRedis;

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
        ShardsRedis redis = new ShardsRedis(
            "172.17.20.73:6380 172.17.20.73:6380",
            60000,
            new HashSelector(),
            isPoolable);
        RedisComand rc = redis.createRedisCommand();
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
