package fengfei.shard.impl;

import redis.clients.util.Hashing;

public class HashPloy extends AbstractPloy {

    protected Hashing hashed = Hashing.MURMUR_HASH;

    @Override
    public int calculate(String key, int size) {
        return Math.abs((Long.valueOf(hashed.hash(String.valueOf(key)) % size).intValue()));
    }

}
