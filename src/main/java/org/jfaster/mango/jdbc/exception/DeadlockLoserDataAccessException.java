package org.jfaster.mango.jdbc.exception;

/**
 * @author ash
 */
public class DeadlockLoserDataAccessException extends PessimisticLockingFailureException {

    public DeadlockLoserDataAccessException(String msg) {
        super(msg);
    }

    public DeadlockLoserDataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
