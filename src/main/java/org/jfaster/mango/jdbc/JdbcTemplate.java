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

import org.jfaster.mango.datasource.DataSourceUtils;
import org.jfaster.mango.exception.jdbc.DataAccessException;
import org.jfaster.mango.exception.jdbc.DataRetrievalFailureException;
import org.jfaster.mango.base.local.CacheLoader;
import org.jfaster.mango.base.local.DoubleCheckCache;
import org.jfaster.mango.base.local.LoadingCache;
import org.jfaster.mango.base.logging.InternalLogger;
import org.jfaster.mango.base.logging.InternalLoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;


/**
 * @author ash
 */
public class JdbcTemplate implements JdbcOperations {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(JdbcTemplate.class);

    @Override
    public <T> T queryForObject(DataSource dataSource, String sql, Object[] args, RowMapper<T> rowMapper)
            throws DataAccessException {

        return executeQuery(dataSource, sql, args, new ObjectResultSetExtractor<T>(rowMapper));
    }

    @Override
    public <T> List<T> queryForList(DataSource dataSource, String sql, Object[] args,
                                    ListSupplier listSupplier, RowMapper<T> rowMapper)
            throws DataAccessException {

        return executeQuery(dataSource, sql, args, new ListResultSetExtractor<T>(listSupplier, rowMapper));
    }

    @Override
    public <T> Set<T> queryForSet(DataSource dataSource, String sql, Object[] args,
                                  SetSupplier setSupplier, RowMapper<T> rowMapper)
            throws DataAccessException {

        return executeQuery(dataSource, sql, args, new SetResultSetExtractor<T>(setSupplier, rowMapper));
    }

    @Override
    public <T> Object queryForArray(DataSource dataSource, String sql, Object[] args, RowMapper<T> rowMapper)
            throws DataAccessException {

        return executeQuery(dataSource, sql, args, new ArrayResultSetExtractor<T>(rowMapper));
    }

    @Override
    public int update(DataSource dataSource, String sql, Object[] args)
            throws DataAccessException {

        return update(dataSource, sql, args, null);
    }

    @Override
    public int update(DataSource dataSource, String sql, Object[] args, GeneratedKeyHolder holder)
            throws DataAccessException {

        Connection conn = DataSourceUtils.getConnection(dataSource);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            boolean needGenerateKey = holder != null;
            ps = needGenerateKey ?
                    conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : // 生成自增key
                    conn.prepareStatement(sql); // 不生成自增key
            setValues(ps, args);

            if (logger.isDebugEnabled()) {
                logger.debug("Executing \"{}\" {}", sql, args);
            }

            int r = ps.executeUpdate();
            if (needGenerateKey) { // 生成自增key
                rs = ps.getGeneratedKeys();
                if (!rs.next()) {
                    throw new DataRetrievalFailureException("Unable to retrieve the generated key. " +
                            "Check that the table has an identity column enabled.");
                }
                Object key = JdbcUtils.getResultSetValue(rs, 1, holder.getKeyClass());
                holder.setKey(key);
            }
            return r;
        } catch (SQLException e) {
            JdbcUtils.closeResultSet(rs);
            rs = null;
            JdbcUtils.closeStatement(ps);
            ps = null;
            DataSourceUtils.releaseConnection(conn, dataSource);
            conn = null;

            throw getExceptionTranslator(dataSource).translate(sql, e);
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    @Override
    public int[] batchUpdate(DataSource dataSource, String sql, List<Object[]> batchArgs)
            throws DataAccessException {

        Connection conn = DataSourceUtils.getConnection(dataSource);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            setBatchValues(ps, batchArgs);

            if (logger.isDebugEnabled()) {
                List<List<Object>> debugBatchArgs = new ArrayList<List<Object>>(batchArgs.size());
                for (Object[] batchArg : batchArgs) {
                    debugBatchArgs.add(Arrays.asList(batchArg));
                }
                logger.debug("Executing \"{}\" {}", sql, debugBatchArgs);
            }

            return ps.executeBatch();
        } catch (SQLException e) {
            JdbcUtils.closeStatement(ps);
            ps = null;
            DataSourceUtils.releaseConnection(conn, dataSource);
            conn = null;

            throw getExceptionTranslator(dataSource).translate(sql, e);
        } finally {
            JdbcUtils.closeStatement(ps);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    @Override
    public int[] batchUpdate(DataSource dataSource, List<String> sqls, List<Object[]> batchArgs)
            throws DataAccessException {

        int size = Math.min(sqls.size(), batchArgs.size());
        int[] r = new int[size];
        Connection conn = DataSourceUtils.getConnection(dataSource);
        try {
            for (int i = 0; i < size; i++) {
                String sql = sqls.get(i);
                Object[] args = batchArgs.get(i);
                PreparedStatement ps = null;
                try {
                    ps = conn.prepareStatement(sql);
                    setValues(ps, args);

                    if (logger.isDebugEnabled()) {
                        logger.debug("Executing \"{}\" {}", sql, args);
                    }

                    r[i] = ps.executeUpdate();
                } catch (SQLException e) {
                    JdbcUtils.closeStatement(ps);
                    ps = null;
                    DataSourceUtils.releaseConnection(conn, dataSource);
                    conn = null;

                    throw getExceptionTranslator(dataSource).translate(sql, e);
                } finally {
                    JdbcUtils.closeStatement(ps);
                }
            }
        } finally {
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
        return r;
    }

    private <T> T executeQuery(DataSource dataSource, String sql, Object[] args, ResultSetExtractor<T> rse)
            throws DataAccessException {

        Connection conn = DataSourceUtils.getConnection(dataSource);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            setValues(ps, args);

            if (logger.isDebugEnabled()) {
                logger.debug("Executing \"{}\" {}", sql, args);
            }

            rs = ps.executeQuery();
            return rse.extractData(rs);
        } catch (SQLException e) {
            JdbcUtils.closeResultSet(rs);
            rs = null;
            JdbcUtils.closeStatement(ps);
            ps = null;
            DataSourceUtils.releaseConnection(conn, dataSource);
            conn = null;

            throw getExceptionTranslator(dataSource).translate(sql, e);
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            DataSourceUtils.releaseConnection(conn, dataSource);
        }
    }

    private void setValues(PreparedStatement ps, Object[] args) throws SQLException {
        int index = 0;
        for (Object arg : args) {
            JdbcUtils.setParameterValue(ps, ++index, arg);
        }
    }

    private void setBatchValues(PreparedStatement ps, List<Object[]> batchArgs) throws SQLException {
        for (Object[] args : batchArgs) {
            int index = 0;
            for (Object arg : args) {
                JdbcUtils.setParameterValue(ps, ++index, arg);
            }
            ps.addBatch();
        }
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

}






















