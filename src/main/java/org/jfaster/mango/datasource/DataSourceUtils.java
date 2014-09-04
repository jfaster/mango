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

package org.jfaster.mango.datasource;

import org.jfaster.mango.exception.CannotGetJdbcConnectionException;
import org.jfaster.mango.exception.IncorrectJdbcConnectionException;
import org.jfaster.mango.exception.TransactionSystemException;
import org.jfaster.mango.transaction.TransactionContext;
import org.jfaster.mango.transaction.TransactionIsolationLevel;
import org.jfaster.mango.transaction.TransactionSynchronizationManager;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author ash
 */
public class DataSourceUtils {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(DataSourceUtils.class);

    public static Connection getConnection(DataSource ds)
            throws TransactionSystemException, CannotGetJdbcConnectionException, IncorrectJdbcConnectionException {
        TransactionContext tc = TransactionSynchronizationManager.getTransactionContext();
        boolean inTransaction = tc != null;
        if (inTransaction) {
            if (tc.getDataSource() != null
                    && tc.getDataSource() != ds) { // 在使用事务的过程中数据源不一致
                throw new TransactionSystemException("Multiple Datasources in transaction");
            }
            Connection conn = tc.getConnection();
            if (conn != null) { // 使用之前的连接
                if (logger.isDebugEnabled()) {
                    logger.debug("Fetching JDBC Connection from TransactionContext");
                }
                return conn;
            }
        }

        if (logger.isDebugEnabled()) {
            logger.debug("Fetching JDBC Connection from DataSource");
        }
        Connection conn;
        try {
            conn = ds.getConnection();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Could not get JDBC Connection", e);
        }
        if (conn == null) {
            throw new CannotGetJdbcConnectionException("Datasource return null Connection");
        }

        if (inTransaction) {
            if (logger.isDebugEnabled()) {
                logger.debug("Registering JDBC Connection to TransactionContext");
            }
            tc.setConnection(conn);
            tc.setDataSource(ds);

            try {
                TransactionIsolationLevel expectedLevel = tc.getLevel();
                if (expectedLevel != TransactionIsolationLevel.DEFAULT) {
                    int previousLevel = conn.getTransactionIsolation();
                    if (previousLevel != expectedLevel.getLevel()) {
                        if (logger.isDebugEnabled()) {
                            logger.debug("Setting isolation level of JDBC Connection to " + expectedLevel.getLevel());
                        }
                        conn.setTransactionIsolation(expectedLevel.getLevel());
                        tc.setPreviousLevel(previousLevel);
                    }
                }

                if (conn.getAutoCommit()) {
                    if (logger.isDebugEnabled()) {
                        logger.debug("Switching JDBC Connection to manual commit");
                    }
                    conn.setAutoCommit(false);
                }
            } catch (SQLException e) {
                throw new IncorrectJdbcConnectionException("Incorrect JDBC Connection");
            }
        } else {
            if (DataSourceMonitor.needCheckAutoCommit(ds)) { // 如果使用事务后，归还conn时，重置autoCommit失败，则需要检测
                try {
                    if (!conn.getAutoCommit()) {
                        conn.setAutoCommit(true);
                    }
                } catch (SQLException e) {
                    logger.error("Could not set autoCommit of JDBC Connection after get Connection, so close it");
                    releaseConnection(conn);
                    throw new CannotGetJdbcConnectionException("Could not set autoCommit of JDBC Connection " +
                            "after get Connection, so close it");
                }
            }
        }

        return conn;
    }

    public static void releaseConnection(Connection conn) {
        try {
            if (TransactionSynchronizationManager.inTransaction()) { // 在事务中不关闭连接，直接返回
                return;
            }
            if (logger.isDebugEnabled()) {
                logger.debug("Returning JDBC Connection to DataSource");
            }
            conn.close();
        } catch (SQLException e) {
            logger.error("Could not close JDBC Connection", e);
        } catch (Throwable e) {
            logger.error("Unexpected exception on closing JDBC Connection", e);
        }
    }

    public static void resetConnectionAfterTransaction(Connection conn, DataSource ds, Integer previousIsolationLevel) {
        try {
            if (logger.isDebugEnabled()) {
                logger.debug("Switching JDBC Connection to auto commit");
            }
            conn.setAutoCommit(true);
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
        }  catch (Throwable e) {
            logger.error("Unexpected exception on resetting isolation level of JDBC Connection after transaction", e);
        }
    }

}
