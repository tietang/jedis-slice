#Jedis Slice for jedis java client#
**Jedis Slice** is a slice framework for jedis.
**jedis** is a java client for Redis, as follow [jedis](https://github.com/xetorthio/jedis).

#Usage#
  example    src/test/java/fengfei/redis/example/SlicedRedisExample.java
**example 1:**

	        SlicedRedis redis = new SlicedRedis(
				"192.168.1.10:6380 192.168.1.10:6380", 60000,
				new HashEqualizer(), true);
		RedisComand rc = redis.createRedisCommand();

		for (int i = 0; i < 10; i++) {
			rc.set("K" + i, "V" + i);
		}
		redis.close();

**example 2:**

		int timeout = 60;
		Plotter plotter = new LoopPlotter();
		Equalizer equalizer = new LongModuleEqualizer();

		equalizer.setPlotter(plotter);
		// slice 0: master:192.168.1.3:6379 slave:192.168.1.4:6379
		// 192.168.1.5:6379
		Slice slice0 = Slice.createSlice(timeout, "172.17.20.16:6379",
				"192.168.1.3:6380", "192.168.1.3:6381");
		equalizer.addSlice(0l, slice0);
		// slice 1: master:192.168.1.6:6379 slave:192.168.1.7:6379
		// 192.168.1.8:6379
		Slice slice1 = Slice.createSlice(timeout, "192.168.1.6:6379",
				"192.168.1.6:6380", "192.168.1.6:6381");
		equalizer.addSlice(1l, slice1);

		SlicedRedis redis = new SlicedRedis(equalizer, isPoolable);
		RedisComand rc = redis.createRedisCommand();
		for (int i = 0; i < 10; i++) {
			rc.set("" + i, "V" + i);
		}
		redis.close();