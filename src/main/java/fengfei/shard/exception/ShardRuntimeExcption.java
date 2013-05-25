package fengfei.shard.exception;

public class ShardRuntimeExcption extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public ShardRuntimeExcption(String message) {
		super(message);
	}

	public ShardRuntimeExcption(String message, Throwable cause) {
		super(message, cause);
	}
}
