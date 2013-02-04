package fengfei.redis.slice;

import java.util.concurrent.atomic.AtomicInteger;

import fengfei.redis.Plotter;

public class LoopPlotter extends AbstractPlotter implements Plotter {
	protected AtomicInteger next = new AtomicInteger();

	@Override
	public int get(byte[] key, int size) {
		return Math.abs(next.getAndIncrement() % size);
	}

}
