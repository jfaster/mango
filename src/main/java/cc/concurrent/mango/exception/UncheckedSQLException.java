package cc.concurrent.mango.exception;

/**
 * Unchecked {@link java.sql.SQLException}
 *
 * @author ash
 */
public class UncheckedSQLException extends DataAccessException {

    public UncheckedSQLException(Throwable cause) {
        super(cause);
    }

}
