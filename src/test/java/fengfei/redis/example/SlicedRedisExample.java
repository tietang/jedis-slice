package fengfei.redis.example;

import fengfei.redis.Equalizer;
import fengfei.redis.Plotter;
import fengfei.redis.RedisComand;
import fengfei.redis.slice.HashEqualizer;
import fengfei.redis.slice.LongModuleEqualizer;
import fengfei.redis.slice.LoopPlotter;
import fengfei.redis.slice.Slice;
import fengfei.redis.slice.SlicedRedis;

public class SlicedRedisExample {
	private static boolean isPoolable = false;

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		isPoolable = true;
//		example1();
//		example2();
		example3();

	}

	public static void example1() {
		SlicedRedis redis = new SlicedRedis(
				"172.17.20.16:6380 172.17.20.16:6380", 60000,
				new HashEqualizer(), isPoolable);
		RedisComand rc = redis.createRedisCommand();

		for (int i = 0; i < 10; i++) {
			rc.set("K" + i, "V" + i);
		}
		redis.close();
	}

	public static void example2() {
		SlicedRedis redis = new SlicedRedis(
				"172.17.20.16:6379 172.17.20.16:6380", 60000,
				new LongModuleEqualizer(), isPoolable);
		RedisComand rc = redis.createRedisCommand();
		for (int i = 0; i < 10; i++) {
			rc.set("" + i, "V" + i);
		}
		redis.close();
	}

	public static void example3() {
		// Equalizer equalizer = new HashEqualizer();
		int timeout = 60;
		Plotter plotter = new LoopPlotter();
		Equalizer equalizer = new LongModuleEqualizer();

		equalizer.setPlotter(new LoopPlotter());
		// slice 0: master:192.168.1.3:6379 slave:192.168.1.4:6379
		// 192.168.1.5:6379
		Slice slice0 = Slice.createSlice(timeout, plotter, "172.17.20.16:6379",
				"172.17.20.16:6380", "172.17.20.16:6381");
		equalizer.addSlice(0l, slice0);
		// slice 1: master:192.168.1.6:6379 slave:192.168.1.7:6379
		// 192.168.1.8:6379
		Slice slice1 = Slice.createSlice(timeout, plotter, "172.17.20.16:6379",
				"172.17.20.16:6380", "172.17.20.16:6381");
		equalizer.addSlice(1l, slice1);

		SlicedRedis redis = new SlicedRedis(equalizer, isPoolable);
		RedisComand rc = redis.createRedisCommand();
		for (int i = 0; i < 10; i++) {
			rc.set("" + i, "V" + i);
		}
		redis.close();
	}
}
