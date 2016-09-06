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

import org.jfaster.mango.transaction.exception.CannotGetJdbcConnectionException;
import org.jfaster.mango.jdbc.exception.MetaDataAccessException;
import org.jfaster.mango.transaction.DataSourceUtils;
import org.jfaster.mango.util.PatternMatchUtils;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ash
 */
public class SQLErrorCodesFactory {

  protected final static InternalLogger logger = InternalLoggerFactory.getInstance(SQLErrorCodesFactory.class);

  private final static SQLErrorCodesFactory instance = new SQLErrorCodesFactory();

  private final Map<DataSource, SQLErrorCodes> dataSourceCache = new HashMap<DataSource, SQLErrorCodes>(16);

  private final Map<String, SQLErrorCodes> errorCodesMap;

  public SQLErrorCodesFactory() {
    this.errorCodesMap = buildErrorCodesMap();
  }

  public static SQLErrorCodesFactory getInstance() {
    return instance;
  }

  public SQLErrorCodes getErrorCodes(DataSource dataSource) {
    if (logger.isDebugEnabled()) {
      logger.debug("Looking up default SQLErrorCodes for DataSource [" + dataSource + "]");
    }

    synchronized (this.dataSourceCache) {
      SQLErrorCodes sec = this.dataSourceCache.get(dataSource);
      if (sec != null) {
        if (logger.isDebugEnabled()) {
          logger.debug("SQLErrorCodes found in cache for DataSource [" +
              dataSource.getClass().getName() + '@' + Integer.toHexString(dataSource.hashCode()) + "]");
        }
        return sec;
      }
      try {
        String dbName = fetchDatabaseProductName(dataSource);
        if (dbName != null) {
          if (logger.isDebugEnabled()) {
            logger.debug("Database product name cached for DataSource [" +
                dataSource.getClass().getName() + '@' + Integer.toHexString(dataSource.hashCode()) +
                "]: name is '" + dbName + "'");
          }
          sec = getErrorCodes(dbName);
          this.dataSourceCache.put(dataSource, sec);
          return sec;
        }
      } catch (MetaDataAccessException ex) {
        logger.warn("Error while extracting database product name - falling back to empty error codes", ex);
      }
    }

    // 失败返回空的SQLErrorCodes
    return SQLErrorCodes.EMPTY;
  }

  private SQLErrorCodes getErrorCodes(String dbName) {
    SQLErrorCodes sec = this.errorCodesMap.get(dbName);
    if (sec == null) {
      for (SQLErrorCodes candidate : this.errorCodesMap.values()) {
        if (PatternMatchUtils.simpleMatch(candidate.getDatabaseProductNames(), dbName)) {
          sec = candidate;
          break;
        }
      }
    }

    if (sec != null) {
      return sec;
    }

    if (logger.isDebugEnabled()) {
      logger.debug("SQL error codes for '" + dbName + "' not found");
    }
    return SQLErrorCodes.EMPTY;
  }

  private Map<String, SQLErrorCodes> buildErrorCodesMap() {
    Map<String, SQLErrorCodes> errorCodesMap = new HashMap<String, SQLErrorCodes>();
    for (SQLErrorCodes errorCodes : SQLErrorCodes.values()) {
      if (errorCodes != SQLErrorCodes.EMPTY) {
        errorCodes.init();
        errorCodesMap.put(errorCodes.name(), errorCodes);
      }
    }
    return errorCodesMap;
  }

  /**
   * 获取数据库名称
   *
   * @param dataSource
   * @return
   * @throws MetaDataAccessException
   */
  private String fetchDatabaseProductName(DataSource dataSource) throws MetaDataAccessException {
    Connection conn = null;
    try {
      conn = DataSourceUtils.getConnection(dataSource);
      DatabaseMetaData metaData = conn.getMetaData();
      return metaData.getDatabaseProductName();
    } catch (CannotGetJdbcConnectionException ex) {
      throw new MetaDataAccessException("Could not get Connection for extracting meta data", ex);
    } catch (SQLException ex) {
      throw new MetaDataAccessException("Error while extracting DatabaseMetaData", ex);
    } finally {
      DataSourceUtils.releaseConnection(conn, dataSource);
    }
  }

}
