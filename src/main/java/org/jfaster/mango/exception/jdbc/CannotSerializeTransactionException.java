package org.jfaster.mango.exception.jdbc;

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
