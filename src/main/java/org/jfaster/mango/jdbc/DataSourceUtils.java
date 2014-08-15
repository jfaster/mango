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

package org.jfaster.mango.jdbc;

import org.jfaster.mango.exception.CannotGetJdbcConnectionException;
import org.jfaster.mango.transaction.TransactionContext;
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

    public static Connection getConnection(DataSource ds) {
        try {
            return doGetConnection(ds);
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Could not get JDBC Connection", e);
        }
    }

    public static void releaseConnection(Connection conn, DataSource ds) {
        try {
            doReleaseConnection(conn, ds);
        } catch (SQLException e) {
            logger.error("Could not close JDBC Connection", e);
        } catch (Throwable e) {
            logger.error("Unexpected exception on closing JDBC Connection", e);
        }
    }

    public static boolean resetConnectionAfterTransaction(Connection conn, Integer previousIsolationLevel) {
        try {
            conn.setAutoCommit(true);
            if (previousIsolationLevel != null) {
                conn.setTransactionIsolation(previousIsolationLevel);
            }
            return true;
        } catch (Throwable e) {
            logger.error("Could not reset JDBC Connection after transaction", e);
            return false;
        }
    }


    private static Connection doGetConnection(DataSource ds) throws SQLException {
        TransactionContext tc = TransactionSynchronizationManager.getTransactionContext();
        if (tc != null) { // 事务
            if (tc.getDataSource() != ds) { // 在使用事务的过程中数据源不一致
                throw new RuntimeException(); // TODO
            }
            Connection conn = tc.getConnection();
            if (conn != null) { // 使用之前的连接
                return conn;
            }
        }

        Connection conn = ds.getConnection(); // throws SQLException
        if (conn == null) {
            throw new RuntimeException(); // TODO
        }

        if (tc != null) { // 事务
            tc.setConnection(conn);
            tc.setDataSource(ds);

            if (conn.getAutoCommit()) { // throws SQLException
                conn.setAutoCommit(false); // throws SQLException
            }

            int previousLevel = conn.getTransactionIsolation(); // throws SQLException
            if (previousLevel != tc.getLevel().getLevel()) {
                conn.setTransactionIsolation(tc.getLevel().getLevel()); // throws SQLException
                tc.setPreviousLevel(previousLevel);
            }
        } else {
            if (!conn.getAutoCommit()) {
                conn.setAutoCommit(true);
            }
        }

        return conn;
    }


    private static void doReleaseConnection(Connection conn, DataSource ds) throws SQLException {
        TransactionContext tc = TransactionSynchronizationManager.getTransactionContext();
        if (tc != null) { // 使用事务
            if (tc.getDataSource() != ds) {
                throw new RuntimeException(); // TODO
            }
            return;
        }
        conn.close();
    }

}
