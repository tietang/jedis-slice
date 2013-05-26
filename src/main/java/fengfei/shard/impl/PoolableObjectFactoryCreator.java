package fengfei.shard.impl;

import org.apache.commons.pool.PoolableObjectFactory;

import fengfei.shard.InstanceInfo;

public interface PoolableObjectFactoryCreator<T> {
	PoolableObjectFactory<T> create(InstanceInfo info);
}
