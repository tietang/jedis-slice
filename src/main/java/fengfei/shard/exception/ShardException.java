package fengfei.shard.exception;

public class ShardException extends Exception {

    private static final long serialVersionUID = 1L;

    public ShardException(String message) {
        super(message);
    }

    public ShardException(String message, Throwable cause) {
        super(message, cause);
    }

//    @Override
//    public synchronized Throwable fillInStackTrace() {
//        return null;// super.fillInStackTrace();
//    }
}
