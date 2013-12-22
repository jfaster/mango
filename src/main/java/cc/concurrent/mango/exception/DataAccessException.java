package cc.concurrent.mango.exception;

/**
 * @author ash
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(Throwable cause) {
        super(cause);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException() {
    }

}
