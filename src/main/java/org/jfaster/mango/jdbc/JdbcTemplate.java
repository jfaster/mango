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

import org.jfaster.mango.exception.ReturnGeneratedKeyException;
import org.jfaster.mango.exception.UncheckedSQLException;
import org.jfaster.mango.datasource.DataSourceUtils;
import org.jfaster.mango.util.ToStringHelper;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

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
    public <T> T queryForObject(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        return executeQuery(ds, sql, args, new ObjectResultSetExtractor<T>(rowMapper));
    }

    @Override
    public <T> List<T> queryForList(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        return executeQuery(ds, sql, args, new ListResultSetExtractor<T>(rowMapper));
    }

    @Override
    public <T> Set<T> queryForSet(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        return executeQuery(ds, sql, args, new SetResultSetExtractor<T>(rowMapper));
    }

    @Override
    public <T> Object queryForArray(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        return executeQuery(ds, sql, args, new ArrayResultSetExtractor<T>(rowMapper));
    }

    @Override
    public int update(DataSource ds, String sql, Object[] args) {
        return update(ds, sql, args, null);
    }

    @Override
    public int update(DataSource ds, String sql, Object[] args, GeneratedKeyHolder holder) {
        Connection conn = DataSourceUtils.getConnection(ds);
        PreparedStatement ps = null;
        ResultSet rs = null;
        Integer r = null;
        Exception ee = null;

        try {
            boolean needGenerateKey = holder != null;
            ps = needGenerateKey ?
                    conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : // 生成自增key
                    conn.prepareStatement(sql); // 不生成自增key
            setValues(ps, args);
            r = ps.executeUpdate();
            if (needGenerateKey) { // 生成自增key
                rs = ps.getGeneratedKeys();
                if (!rs.next()) {
                    throw new ReturnGeneratedKeyException("please check whether the table has auto increment key");
                }
                Object key = JdbcUtils.getResultSetValue(rs, 1, holder.getKeyClass());
                holder.setKey(key);
            }
            return r;
        } catch (SQLException e) {
            ee = e;
            throw new UncheckedSQLException(e.getMessage(), e);
        } finally {
            if (logger.isDebugEnabled()) {
                String tsArgs = ToStringHelper.toStringNoThrow(args);
                if (ee == null) { // 执行成功
                    logger.debug("\"{}\" #args={} #result={}", sql, tsArgs, r);
                } else {
                    logger.debug("[error] \"{}\" #args={} #errorMsg=[{}]", sql, tsArgs, ee.getMessage());
                }
            }

            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            DataSourceUtils.releaseConnection(conn);
        }
    }

    @Override
    public int[] batchUpdate(DataSource ds, String sql, List<Object[]> batchArgs) {
        Connection conn = DataSourceUtils.getConnection(ds);
        PreparedStatement ps = null;
        int[] r = null;
        Exception ee = null;
        try {
            ps = conn.prepareStatement(sql);
            setBatchValues(ps, batchArgs);
            r = ps.executeBatch();
            return r;
        } catch (SQLException e) {
            ee = e;
            throw new UncheckedSQLException(e.getMessage(), e);
        } finally {
            if (logger.isDebugEnabled()) {
                List<List<Object>> debugBatchArgs = new ArrayList<List<Object>>(batchArgs.size());
                for (Object[] batchArg : batchArgs) {
                    debugBatchArgs.add(Arrays.asList(batchArg));
                }
                String tsDebugBatchArgs = ToStringHelper.toStringNoThrow(debugBatchArgs);
                if (ee == null) { // 执行成功
                    logger.debug("\"{}\" #args={} #result={}", sql, tsDebugBatchArgs, r);
                } else {
                    logger.debug("[error] \"{}\" #args={} #errorMsg=[{}]", sql, tsDebugBatchArgs, ee.getMessage());
                }
            }

            JdbcUtils.closeStatement(ps);
            DataSourceUtils.releaseConnection(conn);
        }
    }

    @Override
    public int[] batchUpdate(DataSource ds, List<String> sqls, List<Object[]> batchArgs) {
        int size = Math.min(sqls.size(), batchArgs.size());
        int[] r = new int[size];
        Connection conn = DataSourceUtils.getConnection(ds);
        try {
            for (int i = 0; i < size; i++) {
                String sql = sqls.get(i);
                Object[] args = batchArgs.get(i);
                Exception ee = null;
                PreparedStatement ps = null;
                try {
                    ps = conn.prepareStatement(sql);
                    setValues(ps, args);
                    r[i] = ps.executeUpdate();
                } catch (SQLException e) {
                    ee = e;
                    throw new UncheckedSQLException(e.getMessage(), e);
                } finally {
                    if (logger.isDebugEnabled()) {
                        String tsArgs = ToStringHelper.toStringNoThrow(args);
                        if (ee == null) {
                            logger.debug("\"{}\" #args={} #result={}", sql, tsArgs, r[i]);
                        } else {
                            logger.debug("[error] \"{}\" #args={} #errorMsg=[{}]", sql, tsArgs, ee.getMessage());
                        }
                    }

                    JdbcUtils.closeStatement(ps);
                }
            }
        } finally {
            DataSourceUtils.releaseConnection(conn);
        }
        return r;
    }

    private <T> T executeQuery(DataSource ds, String sql, Object[] args, ResultSetExtractor<T> rse) {
        Connection conn = DataSourceUtils.getConnection(ds);
        PreparedStatement ps = null;
        ResultSet rs = null;
        T r = null;
        Exception ee = null;
        try {
            ps = conn.prepareStatement(sql);
            setValues(ps, args);
            rs = ps.executeQuery();
            r = rse.extractData(rs);
            return r;
        } catch (SQLException e) {
            ee = e;
            throw new UncheckedSQLException(e.getMessage(), e);
        } finally {
            if (logger.isDebugEnabled()) {
                String tsArgs = ToStringHelper.toStringNoThrow(args);
                String tsR = ToStringHelper.toStringNoThrow(r);
                if (ee == null) { // 执行成功
                    logger.debug("\"{}\" #args={} #result={}", sql, tsArgs, tsR);
                } else {
                    logger.debug("[error] \"{}\" #args={} #errorMsg=[{}]", sql, tsArgs, ee.getMessage());
                }
            }

            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            DataSourceUtils.releaseConnection(conn);
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

}






















