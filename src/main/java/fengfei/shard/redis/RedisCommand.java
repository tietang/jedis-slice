package fengfei.shard.redis;

import java.util.List;

import redis.clients.jedis.BinaryJedisCommands;
import redis.clients.jedis.JedisCommands;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.PipelineBlock;

public interface RedisCommand extends JedisCommands, BinaryJedisCommands, JedisOtherCommands {

    // , MultiKeyCommands, AdvancedJedisCommands, ScriptingCommands {
    String flushDB();

    Pipeline pipelined();

    List<Object> pipelined(final PipelineBlock jedisPipeline);
}
