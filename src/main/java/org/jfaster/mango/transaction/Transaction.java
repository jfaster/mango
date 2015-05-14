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

import org.jfaster.mango.datasource.DataSourceMonitor;
import org.jfaster.mango.datasource.DataSourceUtils;
import org.jfaster.mango.transaction.exception.IllegalTransactionStateException;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author ash
 */
public class Transaction {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(Transaction.class);

    private final boolean newTransaction;

    private final DataSource dataSource;

    private final Integer previousLevel;

    private final boolean mustRestoreAutoCommit;

    private boolean completed;

    public Transaction(boolean newTransaction, DataSource dataSource) {
        this(newTransaction, dataSource, null, true);
    }

    public Transaction(boolean newTransaction, DataSource dataSource,
                       Integer previousLevel, boolean mustRestoreAutoCommit) {
        this.newTransaction = newTransaction;
        this.dataSource = dataSource;
        this.previousLevel = previousLevel;
        this.mustRestoreAutoCommit = mustRestoreAutoCommit;
    }

    public void commit() {
        if (isCompleted()) {
            throw new IllegalTransactionStateException(
                    "Transaction is already completed - do not call commit or rollback more than once per transaction");
        }

        ConnectionHolder connHolder = TransactionSynchronizationManager.getConnectionHolder(dataSource);
        if (connHolder == null) {
            throw new IllegalStateException("No ConnectionHolder bind to DataSource [" + dataSource + "]");
        }

        if (!isNewTransaction()) { // 嵌套的事务，不真正提交
            return;
        }

        if (connHolder.isRollbackOnly()) { // 嵌套的事务出现回滚则回滚
            processRollback(connHolder.getConnection(), dataSource);
        }

        // 提交事务
        processCommit(connHolder.getConnection(), dataSource);
    }

    private void processCommit(Connection conn, DataSource dataSource) {
        try {
            doCommit(conn);
        } catch (Exception e) {
            doRollbackOnCommitException(conn, e);
        } finally {
            TransactionSynchronizationManager.unbindConnectionHolder(dataSource);
            resetConnectionAfterTransaction(conn, dataSource, getPreviousLevel(), isMustRestoreAutoCommit());
            DataSourceUtils.releaseConnection(conn, dataSource);
            setCompleted(true);
        }
    }

    private void doCommit(Connection conn) {
        try {
            conn.commit();
        } catch (SQLException e) {
            throw new RuntimeException(); // TODO
        }
    }

    private void doRollbackOnCommitException(Connection conn, Exception ce) {
        try {
            doRollback(conn);
        } catch (Exception re) {
            throw new RuntimeException(); // TODO
        }
    }

    public void rollback() {
        if (isCompleted()) {
            throw new IllegalTransactionStateException(
                    "Transaction is already completed - do not call commit or rollback more than once per transaction");
        }

        ConnectionHolder connHolder = TransactionSynchronizationManager.getConnectionHolder(dataSource);
        if (connHolder == null) {
            throw new IllegalStateException("No ConnectionHolder bind to DataSource [" + dataSource + "]");
        }

        if (!isNewTransaction()) {
            connHolder.setRollbackOnly(true); // 促发顶层事务回滚
            return;
        }

        processRollback(connHolder.getConnection(), dataSource);
    }

    private void processRollback(Connection conn, DataSource dataSource) {
        try {
            doRollback(conn);
        } catch (Exception e) {
            throw new RuntimeException(); // TODO
        } finally {
            TransactionSynchronizationManager.unbindConnectionHolder(dataSource);
            resetConnectionAfterTransaction(conn, dataSource, getPreviousLevel(), isMustRestoreAutoCommit());
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void doRollback(Connection conn) {
        try {
            conn.rollback();
        } catch (SQLException e) {
            throw new RuntimeException(); // TODO
        }
    }

    public boolean isNewTransaction() {
        return newTransaction;
    }

    public DataSource getDataSource() {
        return dataSource;
    }

    public Integer getPreviousLevel() {
        return previousLevel;
    }

    public boolean isMustRestoreAutoCommit() {
        return mustRestoreAutoCommit;
    }

    private boolean isCompleted() {
        return completed;
    }

    private void setCompleted(boolean completed) {
        this.completed = completed;
    }

    private static void resetConnectionAfterTransaction(
            Connection conn, DataSource ds, Integer previousIsolationLevel,
            boolean isMustRestoreAutoCommit) {

        try {
            if (isMustRestoreAutoCommit) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Switching JDBC Connection to auto commit");
                }
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Could not reset autoCommit of JDBC Connection after transaction", e);
            DataSourceMonitor.resetAutoCommitFail(ds);
        } catch (Throwable e) {
            logger.error("Unexpected exception on resetting autoCommit of JDBC Connection after transaction", e);
            DataSourceMonitor.resetAutoCommitFail(ds);
        }
        try {
            if (previousIsolationLevel != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Resetting isolation level of JDBC Connection to " + previousIsolationLevel);
                }
                conn.setTransactionIsolation(previousIsolationLevel);
            }
        } catch (SQLException e) {
            logger.error("Could not reset isolation level of JDBC Connection after transaction", e);
        } catch (Throwable e) {
            logger.error("Unexpected exception on resetting isolation level of JDBC Connection after transaction", e);
        }
    }


}
