package cc.concurrent.mango.jdbc.exception;

/**
 * @author ash
 */
public class CannotGetJdbcConnectionException extends DataAccessException {

    public CannotGetJdbcConnectionException(String message, Throwable cause) {
        super(message, cause);
    }

}
