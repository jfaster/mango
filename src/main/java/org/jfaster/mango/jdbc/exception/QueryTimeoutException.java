package org.jfaster.mango.jdbc.exception;

/**
 * @author ash
 */
public class QueryTimeoutException extends DataAccessException {

    public QueryTimeoutException(String msg) {
        super(msg);
    }

    public QueryTimeoutException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
