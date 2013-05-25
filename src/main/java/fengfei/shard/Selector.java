package fengfei.shard;

import java.util.List;

import fengfei.shard.exception.ShardException;

public interface Selector {

	public final static int ReadWrite = 0;
	public final static int Write = 1;
	public final static int Read = 2;

	InstanceInfo select(String key, int readWrite) throws ShardException;

	InstanceInfo selectMaster(String key) throws ShardException;

	InstanceInfo selectSlave(String key) throws ShardException;

	InstanceInfo selectAny(String key) throws ShardException;

	void addShard(Shard shard);

	void addShard(int index, Shard shard);

	void addInstanceInfo(int index, InstanceInfo info);

	List<Shard> getShards();

	Ploy getPloy();

	void setPloy(Ploy ploy);
}
