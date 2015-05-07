package org.jfaster.mango.jdbc.exception;

/**
 * @author ash
 */
public class CannotAcquireLockException extends PessimisticLockingFailureException {

    public CannotAcquireLockException(String msg) {
        super(msg);
    }

    public CannotAcquireLockException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
