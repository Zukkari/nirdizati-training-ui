package cs.ut.exceptions;

public class NirdizatiRuntimeException extends RuntimeException {

    public NirdizatiRuntimeException() {
        super();
    }

    public NirdizatiRuntimeException(String message) {
        super(message);
    }

    public NirdizatiRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public NirdizatiRuntimeException(Throwable cause) {
        super(cause);
    }

    protected NirdizatiRuntimeException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
