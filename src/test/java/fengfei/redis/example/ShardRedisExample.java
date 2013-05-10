package fengfei.redis.example;

import fengfei.shard.Ploy;
import fengfei.shard.Selector;
import fengfei.shard.Shard;
import fengfei.shard.impl.HashSelector;
import fengfei.shard.impl.LoopPloy;
import fengfei.shard.impl.ModuleSelector;
import fengfei.shard.redis.RedisComand;
import fengfei.shard.redis.ShardRedis;

public class ShardRedisExample {

    private static boolean isPoolable = false;

    /**
     * @param args
     */
    public static void main(String[] args) {
        isPoolable = true;

        // example1();
        example2();
        // example3();

    }

    public static void example1() {
        ShardRedis redis = new ShardRedis(
            "172.17.20.73:6380 172.17.20.73:6380",
            60000,
            new HashSelector(),
            isPoolable);
        RedisComand rc = redis.createRedisCommand();

        for (int i = 0; i < 10; i++) {
            rc.set("K" + i, "V" + i);
        }
        for (int i = 0; i < 10; i++) {
            String v = rc.get("K" + i);
            System.out.println(v);
        }
        redis.close();
    }

    public static void example2() {
        ShardRedis redis = new ShardRedis(
            "172.17.20.73:6379 172.17.20.73:6380 172.17.20.73:6381",
            6,
            new ModuleSelector(),
            isPoolable);
        RedisComand rc = redis.createRedisCommand();
        for (int i = 0; i < 10; i++) {
            rc.set("" + i, "V" + i);
        }
        for (int i = 0; i < 10; i++) {
            String v = rc.get(""+i);
            System.out.println(v);
        }
        redis.close();
    }

    public static void example3() {
        // Selector selector Selector selector = new HashSelector selector();
        int timeout = 6;
        Ploy Ploy = new LoopPloy();
        Selector selector = new ModuleSelector();

        selector.setPloy(Ploy);
        // Shard 0: master:192.738.1.3:6379 slave:192.738.1.4:6379
        // 192.738.1.5:6379
        Shard Shard0 = Shard.createShard(
            timeout,
            "172.17.20.73:6379",
            "172.17.20.73:6380",
            "172.17.20.73:6381");
        selector.addShard(0, Shard0);
        // Shard 1: master:192.738.1.6:6379 slave:192.738.1.7:6379
        // 192.738.1.8:6379
        Shard Shard1 = Shard.createShard(
            timeout,
            "172.17.20.73:6379",
            "172.17.20.73:6380",
            "172.17.20.73:6381");
        selector.addShard(1, Shard1);

        ShardRedis redis = new ShardRedis(selector, isPoolable);
        RedisComand rc = redis.createRedisCommand();
        for (int i = 0; i < 10; i++) {
            rc.set("" + i, "V" + i);
        }
        redis.close();
    }
}
