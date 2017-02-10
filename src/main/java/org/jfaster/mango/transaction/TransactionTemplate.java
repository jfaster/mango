/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.transaction;

import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.transaction.exception.TransactionException;

/**
 * @author ash
 */
public class TransactionTemplate {

  public static void execute(
      Mango mango, String dataSourceFactoryName, TransactionIsolationLevel level,
      TransactionAction action) throws TransactionException {
    execute(TransactionFactory.newTransaction(mango, dataSourceFactoryName, level), action);
  }

  public static void execute(Mango mango, String dataSourceFactoryName, TransactionAction action)
      throws TransactionException {
    execute(TransactionFactory.newTransaction(mango, dataSourceFactoryName), action);
  }

  public static void execute(String dataSourceFactoryName, TransactionIsolationLevel level, TransactionAction action)
      throws TransactionException {
    execute(TransactionFactory.newTransaction(dataSourceFactoryName, level), action);
  }

  public static void execute(String dataSourceFactoryName, TransactionAction action) throws TransactionException {
    execute(TransactionFactory.newTransaction(dataSourceFactoryName), action);
  }

  public static void execute(TransactionIsolationLevel level, TransactionAction action) throws TransactionException {
    execute(TransactionFactory.newTransaction(level), action);
  }

  public static void execute(TransactionAction action) throws TransactionException {
    execute(TransactionFactory.newTransaction(), action);
  }

  private static void execute(Transaction transaction, TransactionAction action) throws TransactionException {
    TransactionStatus status = new TransactionStatus();
    try {
      action.doInTransaction(status);
    } catch (RuntimeException e) {
      transaction.rollback();
      throw e;
    }
    if (status.isRollbackOnly()) {
      transaction.setRollbackOnly(true);
    }
    transaction.commit();
  }

}
