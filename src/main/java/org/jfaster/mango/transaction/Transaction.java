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

import org.jfaster.mango.datasource.DataSourceUtils;
import org.jfaster.mango.transaction.exception.IllegalTransactionStateException;
import org.jfaster.mango.transaction.exception.TransactionSystemException;
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

    private final boolean isNewTransaction;

    private final DataSource dataSource;

    private final Integer previousLevel;

    private final boolean isMustRestoreAutoCommit;

    private boolean isCompleted = false;

    public Transaction(boolean newTransaction, DataSource dataSource) {
        this(newTransaction, dataSource, null, true);
    }

    public Transaction(boolean isNewTransaction, DataSource dataSource,
                       Integer previousLevel, boolean isMustRestoreAutoCommit) {
        this.isNewTransaction = isNewTransaction;
        this.dataSource = dataSource;
        this.previousLevel = previousLevel;
        this.isMustRestoreAutoCommit = isMustRestoreAutoCommit;
    }

    public void commit() {
        if (isCompleted) {
            throw new IllegalTransactionStateException(
                    "Transaction is already completed - do not call commit or rollback more than once per transaction");
        }

        ConnectionHolder connHolder = TransactionSynchronizationManager.getConnectionHolder(dataSource);
        if (connHolder == null) {
            throw new IllegalStateException("No ConnectionHolder bind to DataSource [" + dataSource + "]");
        }

        if (!isNewTransaction) { // 嵌套的事务，不真正提交
            if (logger.isDebugEnabled()) {
                logger.debug("Not a new transaction, Not really commit");
            }
            return;
        }

        if (connHolder.isRollbackOnly()) { // 嵌套的事务出现回滚则回滚
            if (logger.isDebugEnabled()) {
                logger.debug("Transaction is marked as rollback-only, so will rollback");
            }
            processRollback(connHolder.getConnection());
        }

        // 提交事务
        processCommit(connHolder.getConnection());
    }

    private void processCommit(Connection conn) {
        try {
            doCommit(conn);
        } catch (Exception e) {
            doRollbackOnCommitException(conn, e);
        } finally {
            cleanup(conn);
        }
    }

    private void doCommit(Connection conn) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Committing JDBC transaction on Connection [" + conn + "]");
            }
            conn.commit();
        } catch (SQLException e) {
            throw new TransactionSystemException("Could not commit JDBC transaction", e);
        }
    }

    private void doRollbackOnCommitException(Connection conn, Exception e) {
        try {
            doRollback(conn);
        } catch (TransactionSystemException tes) {
            logger.error("Commit exception overridden by rollback exception", e);
            throw tes;
        }
    }

    public void rollback() {
        if (isCompleted) {
            throw new IllegalTransactionStateException(
                    "Transaction is already completed - do not call commit or rollback more than once per transaction");
        }

        ConnectionHolder connHolder = TransactionSynchronizationManager.getConnectionHolder(dataSource);
        if (connHolder == null) {
            throw new IllegalStateException("No ConnectionHolder bind to DataSource [" + dataSource + "]");
        }

        if (!isNewTransaction) {
            if (logger.isDebugEnabled()) {
                logger.debug("Not a new transaction, Not really rollback");
            }
            connHolder.setRollbackOnly(true); // 促发顶层事务回滚
            return;
        }

        processRollback(connHolder.getConnection());
    }

    private void processRollback(Connection conn) {
        try {
            doRollback(conn);
        } finally {
            cleanup(conn);
        }
    }

    private void doRollback(Connection conn) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Rolling back JDBC transaction on Connection [" + conn + "]");
            }
            conn.rollback();
        } catch (SQLException e) {
            throw new TransactionSystemException("Could not roll back JDBC transaction", e);
        }
    }

    private void cleanup(Connection conn) {
        TransactionSynchronizationManager.unbindConnectionHolder(dataSource);
        resetConnectionAfterTransaction(conn);
        DataSourceUtils.releaseConnection(conn, dataSource);
        isCompleted = true;
    }

    private void resetConnectionAfterTransaction(Connection conn) {

        try {
            if (isMustRestoreAutoCommit) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Switching JDBC Connection to auto commit");
                }
                conn.setAutoCommit(true);
            }
        } catch (SQLException e) {
            logger.error("Could not reset autoCommit of JDBC Connection after transaction", e);
        } catch (Throwable e) {
            logger.error("Unexpected exception on resetting autoCommit of JDBC Connection after transaction", e);
        }
        try {
            if (previousLevel != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("Resetting isolation level of JDBC Connection to " + previousLevel);
                }
                conn.setTransactionIsolation(previousLevel);
            }
        } catch (SQLException e) {
            logger.error("Could not reset isolation level of JDBC Connection after transaction", e);
        } catch (Throwable e) {
            logger.error("Unexpected exception on resetting isolation level of JDBC Connection after transaction", e);
        }
    }


}
