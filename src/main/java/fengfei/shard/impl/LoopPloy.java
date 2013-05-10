package fengfei.shard.impl;

import java.util.concurrent.atomic.AtomicInteger;

public class LoopPloy extends AbstractPloy {

    protected AtomicInteger next = new AtomicInteger();

    @Override
    public int calculate(String key, int size) {
        return Math.abs(next.getAndIncrement() % size);
    }

}
