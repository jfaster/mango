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

import org.jfaster.mango.transaction.exception.CannotGetJdbcConnectionException;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;

/**
 * @author ash
 */
public class DataSourceUtils {

  private final static InternalLogger logger = InternalLoggerFactory.getInstance(DataSourceUtils.class);

  public static Connection getConnection(DataSource dataSource) throws CannotGetJdbcConnectionException {
    try {
      return doGetConnection(dataSource);
    } catch (SQLException e) {
      throw new CannotGetJdbcConnectionException("Could not get JDBC Connection", e);
    }
  }

  private static Connection doGetConnection(DataSource dataSource) throws SQLException {
    ConnectionHolder connHolder = TransactionSynchronizationManager.getConnectionHolder(dataSource);
    if (connHolder != null) {
      if (logger.isDebugEnabled()) {
        logger.debug("Fetching resumed JDBC Connection from DataSource");
      }
      return connHolder.getConnection();
    }
    if (logger.isDebugEnabled()) {
      logger.debug("Fetching JDBC Connection from DataSource");
    }
    Connection conn = dataSource.getConnection();
    if (DataSourceMonitor.needCheckAutoCommit(dataSource)) { // 如果使用事务后，归还conn时，重置autoCommit失败，则需要检测
      try {
        if (!conn.getAutoCommit()) {
          conn.setAutoCommit(true);
        }
      } catch (Throwable e) {
        logger.error("Could not set autoCommit of JDBC Connection after get Connection, so close it", e);
        releaseConnection(conn, dataSource);
        throw new CannotGetJdbcConnectionException("Could not set autoCommit of JDBC Connection " +
            "after get Connection, so close it", e);
      }
    }
    return conn;
  }


  public static void releaseConnection(@Nullable Connection conn, DataSource dataSource) {
    try {
      doReleaseConnection(conn, dataSource);
    } catch (SQLException e) {
      logger.error("Could not close JDBC Connection", e);
    } catch (Throwable e) {
      logger.error("Unexpected exception on closing JDBC Connection", e);
    }
  }

  private static void doReleaseConnection(@Nullable Connection conn, DataSource dataSource) throws SQLException {
    if (conn == null) {
      return;
    }
    ConnectionHolder connHolder = TransactionSynchronizationManager.getConnectionHolder(dataSource);
    if (connHolder != null && connectionEquals(connHolder, conn)) {
      return;
    }
    logger.debug("Returning JDBC Connection to DataSource");
    conn.close();
  }

  private static boolean connectionEquals(ConnectionHolder connHolder, Connection passedInConn) {
    Connection heldConn = connHolder.getConnection();
    return heldConn == passedInConn || heldConn.equals(passedInConn);
  }

}
