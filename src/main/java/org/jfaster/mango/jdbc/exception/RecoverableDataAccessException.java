package org.jfaster.mango.jdbc.exception;

/**
 * @author ash
 * @see java.sql.SQLRecoverableException
 */
public class RecoverableDataAccessException extends DataAccessException {

    public RecoverableDataAccessException(String msg) {
        super(msg);
    }

    public RecoverableDataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
