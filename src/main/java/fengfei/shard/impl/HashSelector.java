package fengfei.shard.impl;

import fengfei.shard.*;
import fengfei.shard.exception.NoAvailableInstanceException;
import fengfei.shard.exception.ShardException;
import redis.clients.util.Hashing;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class HashSelector implements Selector {

    protected List<Shard> shards = new ArrayList<>();
    protected Lock lock = new ReentrantLock();
    protected Ploy ploy = new LoopPloy();
    protected Hashing hashed = Hashing.MD5;
    protected FailoverType failoverType = FailoverType.Exception;

    public HashSelector() {
        super();

    }

    public HashSelector(Ploy ploy) {
        super();
        this.ploy = ploy;
    }

    public void setFailoverType(FailoverType failoverType) {
        this.failoverType = failoverType;
    }

    public void addShard(Shard shard) {
        int index = shards.size();
        shard.setId(index);
        shards.add(shard);
    }

    public void addShard(int index, Shard shard) {
        shard.setId(index);
        shards.add(index, shard);
    }

    public void addInstanceInfo(int index, InstanceInfo info) {
        lock.lock();
        try {
            Shard shard = null;
            if (index >= shards.size()) {
                InstanceInfo master = info.isMaster() ? info : null;
                List<InstanceInfo> slaves = new ArrayList<>();
                if (!info.isMaster()) {
                    slaves.add(info);
                }
                shard = new Shard(index, master, slaves);
            } else {
                shard = shards.get(index);
            }

            shard.addInstanceInfo(info);
        } finally {
            lock.unlock();
        }
    }

    public int calculate(String key, int shardSize) {
        int index = (int) Math.abs(hashed.hash(key) % shardSize);
        return index;
    }

    public Shard selectShard(int key) {
        return shards.get(key);
    }

    private static int selectIndex(List<Range[]> list, long key) {

        for (int j = 0; j < list.size(); j++) {
            Range[] ranges = list.get(j);
            for (Range range : ranges) {
                if (key >= range.start && key <= range.end) {
                    return j;
                }
            }
        }
        return -1;
    }

    @Override
    public InstanceInfo select(String key, int readWrite) throws ShardException {

        int size = shards.size();
        int index = calculate(key, size);

        InstanceInfo info = selectInstanceInfo(index, key, readWrite);

        return info;

    }

    public InstanceInfo selectInstanceInfo(int index, String key, int readWrite)
            throws ShardException {
        try {
            Shard shard = selectShard(index);

            if (shard == null) {
                throw new ShardException("can't find shard for key: " + key);
            }
            InstanceInfo info = getPloy().select(key, shard, readWrite);

            return info;
        } catch (NoAvailableInstanceException e) {
            switch (failoverType) {
                case Exception:
                    throw e;
                case Next:
                    int idx = index + 1;
                    if (idx > shards.size()) {
                        idx = 0;
                    }
                    return selectInstanceInfo(idx, key, readWrite);
                default:
                    throw e;
            }
        }

    }

    @Override
    public InstanceInfo selectMaster(String key) throws ShardException {

        return select(key, Write);

    }

    @Override
    public InstanceInfo selectSlave(String key) throws ShardException {
        return select(key, Read);

    }

    @Override
    public InstanceInfo selectAny(String key) throws ShardException {
        return select(key, ReadWrite);
    }

    public Ploy getPloy() {
        return ploy;
    }

    @Override
    public void setPloy(Ploy ploy) {
        this.ploy = ploy;
    }

    public List<Shard> getShards() {
        return shards;
    }

}
