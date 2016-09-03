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

import org.jfaster.mango.binding.BoundSql;
import org.jfaster.mango.jdbc.exception.DataAccessException;
import org.jfaster.mango.jdbc.exception.DataRetrievalFailureException;
import org.jfaster.mango.mapper.RowMapper;
import org.jfaster.mango.transaction.DataSourceUtils;
import org.jfaster.mango.type.TypeHandler;
import org.jfaster.mango.util.local.CacheLoader;
import org.jfaster.mango.util.local.DoubleCheckCache;
import org.jfaster.mango.util.local.LoadingCache;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * @author ash
 */
public class JdbcTemplate implements JdbcOperations {

  private final static InternalLogger logger = InternalLoggerFactory.getInstance(JdbcTemplate.class);

  @Override
  public <T> T queryForObject(DataSource dataSource, BoundSql boundSql, RowMapper<T> rowMapper)
      throws DataAccessException {

    return executeQuery(dataSource, boundSql, new ObjectResultSetExtractor<T>(rowMapper));
  }

  @Override
  public <T> List<T> queryForList(DataSource dataSource, BoundSql boundSql,
                                  ListSupplier listSupplier, RowMapper<T> rowMapper)
      throws DataAccessException {

    return executeQuery(dataSource, boundSql, new ListResultSetExtractor<T>(listSupplier, rowMapper));
  }

  @Override
  public <T> Set<T> queryForSet(DataSource dataSource, BoundSql boundSql,
                                SetSupplier setSupplier, RowMapper<T> rowMapper)
      throws DataAccessException {

    return executeQuery(dataSource, boundSql, new SetResultSetExtractor<T>(setSupplier, rowMapper));
  }

  @Override
  public <T> Object queryForArray(DataSource dataSource, BoundSql boundSql, RowMapper<T> rowMapper)
      throws DataAccessException {

    return executeQuery(dataSource, boundSql, new ArrayResultSetExtractor<T>(rowMapper));
  }

  @Override
  public int update(DataSource dataSource, BoundSql boundSql)
      throws DataAccessException {

    return update(dataSource, boundSql, null);
  }

  @Override
  public int update(DataSource dataSource, BoundSql boundSql, GeneratedKeyHolder holder)
      throws DataAccessException {

    Connection conn = DataSourceUtils.getConnection(dataSource);
    PreparedStatement ps = null;
    ResultSet rs = null;
    String sql = boundSql.getSql();
    try {
      boolean needGenerateKey = holder != null;
      ps = needGenerateKey ?
          conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : // 生成自增key
          conn.prepareStatement(sql); // 不生成自增key
      setValues(ps, boundSql);

      if (logger.isDebugEnabled()) {
        logger.debug("Executing \"{}\" {}", sql, boundSql.getArgs());
      }

      int r = ps.executeUpdate();
      if (needGenerateKey) { // 生成自增key
        rs = ps.getGeneratedKeys();
        if (!rs.next()) {
          throw new DataRetrievalFailureException("Unable to retrieve the generated key. " +
              "Check that the table has an identity column enabled.");
        }
        Number key = holder.getTypeHandler().getResult(rs, 1);
        holder.setKey(key);
      }
      return r;
    } catch (SQLException e) {
      closeResultSet(rs);
      rs = null;
      closeStatement(ps);
      ps = null;
      DataSourceUtils.releaseConnection(conn, dataSource);
      conn = null;

      throw getExceptionTranslator(dataSource).translate(sql, e);
    } finally {
      closeResultSet(rs);
      closeStatement(ps);
      DataSourceUtils.releaseConnection(conn, dataSource);
    }
  }

  @Override
  public int[] batchUpdate(DataSource dataSource, List<BoundSql> boundSqls) throws DataAccessException {
    return isUniqueSql(boundSqls) ?
        batchUpdateForUniqueSql(dataSource, boundSqls) :
        batchUpdateForDifferentSql(dataSource, boundSqls);
  }

  private <T> T executeQuery(DataSource dataSource, BoundSql boundSql, ResultSetExtractor<T> rse)
      throws DataAccessException {

    Connection conn = DataSourceUtils.getConnection(dataSource);
    PreparedStatement ps = null;
    ResultSet rs = null;
    String sql = boundSql.getSql();
    try {
      ps = conn.prepareStatement(sql);
      setValues(ps, boundSql);

      if (logger.isDebugEnabled()) {
        logger.debug("Executing \"{}\" {}", sql, boundSql.getArgs());
      }

      rs = ps.executeQuery();
      return rse.extractData(rs);
    } catch (SQLException e) {
      closeResultSet(rs);
      rs = null;
      closeStatement(ps);
      ps = null;
      DataSourceUtils.releaseConnection(conn, dataSource);
      conn = null;

      throw getExceptionTranslator(dataSource).translate(sql, e);
    } finally {
      closeResultSet(rs);
      closeStatement(ps);
      DataSourceUtils.releaseConnection(conn, dataSource);
    }
  }

  private int[] batchUpdateForUniqueSql(DataSource dataSource, List<BoundSql> boundSqls)
      throws DataAccessException {
    Connection conn = DataSourceUtils.getConnection(dataSource);
    PreparedStatement ps = null;
    String sql = boundSqls.get(0).getSql();
    try {
      ps = conn.prepareStatement(sql);
      setBatchValues(ps, boundSqls);

      if (logger.isDebugEnabled()) {
        List<List<Object>> debugBatchArgs = new ArrayList<List<Object>>(boundSqls.size());
        for (BoundSql boundSql : boundSqls) {
          debugBatchArgs.add(boundSql.getArgs());
        }
        logger.debug("Executing \"{}\" {}", sql, debugBatchArgs);
      }

      return ps.executeBatch();
    } catch (SQLException e) {
      closeStatement(ps);
      ps = null;
      DataSourceUtils.releaseConnection(conn, dataSource);
      conn = null;

      throw getExceptionTranslator(dataSource).translate(sql, e);
    } finally {
      closeStatement(ps);
      DataSourceUtils.releaseConnection(conn, dataSource);
    }
  }

  private int[] batchUpdateForDifferentSql(DataSource dataSource, List<BoundSql> boundSqls)
      throws DataAccessException {
    int size = boundSqls.size();
    int[] r = new int[size];
    Connection conn = DataSourceUtils.getConnection(dataSource);
    try {
      for (int i = 0; i < size; i++) {
        BoundSql boundSql = boundSqls.get(i);
        String sql = boundSql.getSql();
        PreparedStatement ps = null;
        try {
          ps = conn.prepareStatement(sql);
          setValues(ps, boundSql);

          if (logger.isDebugEnabled()) {
            logger.debug("Executing \"{}\" {}", sql, boundSql.getArgs());
          }

          r[i] = ps.executeUpdate();
        } catch (SQLException e) {
          closeStatement(ps);
          ps = null;
          DataSourceUtils.releaseConnection(conn, dataSource);
          conn = null;

          throw getExceptionTranslator(dataSource).translate(sql, e);
        } finally {
          closeStatement(ps);
        }
      }
    } finally {
      DataSourceUtils.releaseConnection(conn, dataSource);
    }
    return r;
  }

  @SuppressWarnings("unchecked")
  private void setValues(PreparedStatement ps, BoundSql boundSql) throws SQLException {
    List<Object> args = boundSql.getArgs();
    List<TypeHandler<?>> typeHandlers = boundSql.getTypeHandlers();
    for (int i = 0; i < args.size(); i++) {
      TypeHandler typeHandler;
      typeHandler = typeHandlers.get(i);
      Object value = args.get(i);
      typeHandler.setParameter(ps, i + 1, value);
    }
  }

  private void setBatchValues(PreparedStatement ps, List<BoundSql> boundSqls) throws SQLException {
    for (BoundSql boundSql : boundSqls) {
      setValues(ps, boundSql);
      ps.addBatch();
    }
  }

  boolean isUniqueSql(List<BoundSql> boundSqls) {
    String sql = boundSqls.get(0).getSql();
    boolean r = true;
    for (int i = 1; i < boundSqls.size(); i++) {
      if (!sql.equals(boundSqls.get(i).getSql())) {
        r = false;
        break;
      }
    }
    return r;
  }

  private SQLExceptionTranslator getExceptionTranslator(DataSource dataSource) {
    return exceptionTranslatorCache.get(dataSource);
  }

  private final LoadingCache<DataSource, SQLExceptionTranslator> exceptionTranslatorCache
      = new DoubleCheckCache<DataSource, SQLExceptionTranslator>(
      new CacheLoader<DataSource, SQLExceptionTranslator>() {
        public SQLExceptionTranslator load(DataSource dataSource) {
          return new SQLErrorCodeSQLExceptionTranslator(dataSource);
        }
      });

  /**
   * 关闭语句
   *
   * @param stmt
   */
  private void closeStatement(Statement stmt) {
    if (stmt != null) {
      try {
        stmt.close();
      } catch (SQLException e) {
        logger.error("Could not close JDBC Statement", e);
      } catch (Throwable e) {
        logger.error("Unexpected exception on closing JDBC Statement", e);
      }
    }
  }

  /**
   * 关闭结果集
   *
   * @param rs
   */
  private void closeResultSet(ResultSet rs) {
    if (rs != null) {
      try {
        rs.close();
      } catch (SQLException e) {
        logger.error("Could not close JDBC ResultSet", e);
      } catch (Throwable e) {
        logger.error("Unexpected exception on closing JDBC ResultSet", e);
      }
    }
  }

}

