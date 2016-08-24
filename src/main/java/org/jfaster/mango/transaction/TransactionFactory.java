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
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.exception.transaction.CannotCreateTransactionException;
import org.jfaster.mango.base.logging.InternalLogger;
import org.jfaster.mango.base.logging.InternalLoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;

/**
 * @author ash
 */
public abstract class TransactionFactory {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(TransactionFactory.class);

    public static Transaction newTransaction(Mango mango, String database, TransactionIsolationLevel level) {
        DataSource dataSource = mango.getMasterDataSource(database);
        if (dataSource == null) {
            throw new IllegalArgumentException("Can't find master DataSource from mango [" + mango + "] " +
                    "with database [" + database + "]");
        }
        return newTransaction(dataSource, level);
    }

    public static Transaction newTransaction(Mango mango, String database) {
        return newTransaction(mango, database, TransactionIsolationLevel.DEFAULT);
    }

    public static Transaction newTransaction(String database, TransactionIsolationLevel level) {
        List<Mango> mangos = Mango.getInstances();
        if (mangos.size() != 1) {
            throw new IllegalStateException("The number of instances mango expected 1 but " + mangos.size() + ", " +
                    "Please specify mango instance");
        }
        return newTransaction(mangos.get(0), database, level);
    }

    public static Transaction newTransaction(String database) {
        return newTransaction(database, TransactionIsolationLevel.DEFAULT);
    }

    public static Transaction newTransaction(TransactionIsolationLevel level) {
        return newTransaction("", level);
    }

    public static Transaction newTransaction() {
        return newTransaction("", TransactionIsolationLevel.DEFAULT);
    }

    private static Transaction newTransaction(DataSource dataSource, TransactionIsolationLevel level) {
        if (dataSource == null) {
            throw new IllegalArgumentException("DataSource can't be null");
        }
        if (level == null) {
            throw new IllegalArgumentException("TransactionIsolationLevel can't be null");
        }
        ConnectionHolder connHolder = TransactionSynchronizationManager.getConnectionHolder(dataSource);
        return connHolder != null ?
                usingExistingTransaction(dataSource) :
                createNewTransaction(dataSource, level);
    }

    private static Transaction usingExistingTransaction(DataSource dataSource) {
        if (logger.isDebugEnabled()) {
            logger.debug("Using existing transaction");
        }
        Transaction transaction = new TransactionImpl(false, dataSource);
        return transaction;
    }

    private static Transaction createNewTransaction(DataSource dataSource, TransactionIsolationLevel expectedLevel) {
        if (logger.isDebugEnabled()) {
            logger.debug("Creating new transaction");
        }
        Connection conn = null;
        try {
            Integer previousLevel = null;
            boolean isMustRestoreAutoCommit = false;
            conn = dataSource.getConnection();
            if (logger.isDebugEnabled()) {
                logger.debug("Acquired Connection [" + conn + "] for JDBC transaction");
            }

            // 设置事务的隔离级别
            if (expectedLevel != TransactionIsolationLevel.DEFAULT) {
                previousLevel = conn.getTransactionIsolation();
                if (previousLevel != expectedLevel.getLevel()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Changing isolation level of JDBC Connection [" + conn + "] to " +
                                expectedLevel.getLevel());
                    }
                    conn.setTransactionIsolation(expectedLevel.getLevel());
                }
            }

            // 设置自动提交为false
            if (conn.getAutoCommit()) {
                isMustRestoreAutoCommit = true;
                if (logger.isDebugEnabled()) {
                    logger.debug("Switching JDBC Connection [" + conn + "] to manual commit");
                }
                conn.setAutoCommit(false);
            }

            Transaction transaction = new TransactionImpl(true, dataSource, previousLevel, isMustRestoreAutoCommit);
            ConnectionHolder connHolder = new ConnectionHolder(conn);
            TransactionSynchronizationManager.bindConnectionHolder(dataSource, connHolder);
            return transaction;
        } catch (Throwable e) {
            DataSourceUtils.releaseConnection(conn, dataSource);
            throw new CannotCreateTransactionException("Could not open JDBC Connection for transaction", e);
        }
    }

}
