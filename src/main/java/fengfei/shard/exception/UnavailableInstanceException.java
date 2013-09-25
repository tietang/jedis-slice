package fengfei.shard.exception;

public class UnavailableInstanceException extends ShardException {

    private static final long serialVersionUID = 1L;
    public UnavailableInstanceException(String message) {
        super(message);
    }
    @Override
    public synchronized Throwable fillInStackTrace() {
        return null;
    }
}
