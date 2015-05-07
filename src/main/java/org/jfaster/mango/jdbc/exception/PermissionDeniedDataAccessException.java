package org.jfaster.mango.jdbc.exception;

/**
 * @author ash
 */
public class PermissionDeniedDataAccessException extends NonTransientDataAccessException {

    public PermissionDeniedDataAccessException(String msg) {
        super(msg);
    }

    public PermissionDeniedDataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
