package fengfei.shard;

public class ShardException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ShardException(String message) {
		super(message);
	}

	public ShardException(String message, Throwable cause) {
		super(message, cause);
	}
}
