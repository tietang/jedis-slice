package fengfei.slice.impl;

import java.util.Random;

import fengfei.slice.Plotter;

public class RandomPlootter extends AbstractPlotter implements Plotter {
	protected Random random = new Random(19791216);

	@Override
	public int get(byte[] key, int size) {

		return Math.abs(random.nextInt() % size);
	}

}
