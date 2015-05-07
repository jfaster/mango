package org.jfaster.mango.jdbc.exception;

/**
 * @author ash
 */
public class UncategorizedDataAccessException extends NonTransientDataAccessException {

    public UncategorizedDataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
