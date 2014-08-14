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

    private static Connection doGetConnection(DataSource ds) throws SQLException {
        TransactionContext tc = TransactionSynchronizationManager.getTransactionContext();
        if (tc != null) { // 使用事务
            if (tc.getDataSource() != ds) {
                throw new RuntimeException(); // TODO
            }
            Connection conn = tc.getConnection();
            if (conn != null) { // 使用之前的连接
                return conn;
            }
        }

        Connection conn = ds.getConnection();

        if (tc != null) { // 使用事务
            tc.set(ds, conn);
            if (conn.getAutoCommit()) {
                conn.setAutoCommit(false);
            }
            conn.setTransactionIsolation(tc.getLevel().getLevel());
        } else {
            if (!conn.getAutoCommit()) {
                conn.setAutoCommit(true);
            }
            if (conn.getTransactionIsolation() != Connection.TRANSACTION_NONE) {
                conn.setTransactionIsolation(Connection.TRANSACTION_NONE);
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
