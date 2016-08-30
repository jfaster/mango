package org.jfaster.mango.transaction;

/**
 * @author ash
 */
public interface TransactionAction {

  void doInTransaction(TransactionStatus status);

}
