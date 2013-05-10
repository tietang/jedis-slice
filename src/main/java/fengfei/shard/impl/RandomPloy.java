package fengfei.shard.impl;

import java.util.Random;

public class RandomPloy extends AbstractPloy {

    protected Random random = new Random(19791216);

    @Override
    public int calculate(String key, int size) {
        return Math.abs(random.nextInt() % size);
    }

}
