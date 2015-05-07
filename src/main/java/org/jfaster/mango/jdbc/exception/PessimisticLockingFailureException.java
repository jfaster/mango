package org.jfaster.mango.jdbc.exception;

/**
 * @author ash
 * @see CannotAcquireLockException
 * @see DeadlockLoserDataAccessException
 * @see OptimisticLockingFailureException
 */
public class PessimisticLockingFailureException extends ConcurrencyFailureException {

    public PessimisticLockingFailureException(String msg) {
        super(msg);
    }

    public PessimisticLockingFailureException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
