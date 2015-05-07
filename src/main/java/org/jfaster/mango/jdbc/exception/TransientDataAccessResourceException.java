package org.jfaster.mango.jdbc.exception;

/**
 * @author ash
 * @see java.sql.SQLTransientConnectionException
 */
public class TransientDataAccessResourceException extends TransientDataAccessException {

    public TransientDataAccessResourceException(String msg) {
        super(msg);
    }

    public TransientDataAccessResourceException(String msg, Throwable cause) {
        super(msg, cause);
    }

}

