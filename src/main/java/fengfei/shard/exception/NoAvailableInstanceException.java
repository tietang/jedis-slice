package fengfei.shard.exception;

public class NoAvailableInstanceException extends ShardException {

    private static final long serialVersionUID = 1L;
    public NoAvailableInstanceException(String message) {
        super(message);
    }
//    @Override
//    public synchronized Throwable fillInStackTrace() {
//        return null;
//    }
}
