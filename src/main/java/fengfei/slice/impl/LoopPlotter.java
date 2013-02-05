package fengfei.slice.impl;

import java.util.concurrent.atomic.AtomicInteger;

import fengfei.slice.Plotter;

public class LoopPlotter extends AbstractPlotter implements Plotter {
	protected AtomicInteger next = new AtomicInteger();

	@Override
	public int get(byte[] key, int size) {
		return Math.abs(next.getAndIncrement() % size);
	}

}
