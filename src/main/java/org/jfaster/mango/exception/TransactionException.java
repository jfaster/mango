package org.jfaster.mango.exception;

/**
 * @author ash
 */
public abstract class TransactionException extends NestedRuntimeException {

    public TransactionException(String msg) {
        super(msg);
    }

    public TransactionException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
