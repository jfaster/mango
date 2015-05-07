package org.jfaster.mango.jdbc.exception;

/**
 * @author ash
 */
public class InvalidDataAccessApiUsageException extends NonTransientDataAccessException {

    public InvalidDataAccessApiUsageException(String msg) {
        super(msg);
    }

    public InvalidDataAccessApiUsageException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
