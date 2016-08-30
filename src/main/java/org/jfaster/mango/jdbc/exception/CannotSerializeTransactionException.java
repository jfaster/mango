package org.jfaster.mango.jdbc.exception;

/**
 * @author ash
 */
public class CannotSerializeTransactionException extends PessimisticLockingFailureException {

  public CannotSerializeTransactionException(String msg) {
    super(msg);
  }

  public CannotSerializeTransactionException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
