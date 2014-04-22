package cc.concurrent.mango.exception;

/**
 * Unchecked {@link java.sql.SQLException}
 *
 * @author ash
 */
public class UncheckedSQLException extends DataAccessException {

    public UncheckedSQLException(String message, Throwable cause) {
        super(message, cause);
    }

}
