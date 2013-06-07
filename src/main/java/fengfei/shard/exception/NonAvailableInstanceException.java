package fengfei.shard.exception;

public class NonAvailableInstanceException extends ShardException {

    private static final long serialVersionUID = 1L;
    public NonAvailableInstanceException(String message) {
        super(message);
    }
    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
