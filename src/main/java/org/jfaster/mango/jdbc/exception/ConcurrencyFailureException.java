package org.jfaster.mango.jdbc.exception;

/**
 * @author ash
 * @see OptimisticLockingFailureException
 * @see PessimisticLockingFailureException
 * @see CannotAcquireLockException
 * @see DeadlockLoserDataAccessException
 */
public class ConcurrencyFailureException extends DataAccessException {

    public ConcurrencyFailureException(String msg) {
        super(msg);
    }

    public ConcurrencyFailureException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
