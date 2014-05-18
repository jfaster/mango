/*
 * Copyright 2014 mango.concurrent.cc
 *
 * The Netty Project licenses this file to you under the Apache License,
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

package cc.concurrent.mango.jdbc;

import cc.concurrent.mango.exception.ReturnGeneratedKeyException;
import cc.concurrent.mango.exception.UncheckedSQLException;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;
import java.util.Set;


/**
 * @author ash
 */
public class JdbcTemplate {

    public <T> T queryForObject(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        return executeQuery(ds, sql, args, new ObjectResultSetExtractor<T>(rowMapper));
    }

    public <T> List<T> queryForList(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        return executeQuery(ds, sql, args, new ListResultSetExtractor<T>(rowMapper));
    }

    public <T> Set<T> queryForSet(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        return executeQuery(ds, sql, args, new SetResultSetExtractor<T>(rowMapper));
    }

    public <T> Object queryForArray(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        return executeQuery(ds, sql, args, new ArrayResultSetExtractor<T>(rowMapper));
    }

    public int update(DataSource ds, String sql, Object[] args) {
        return update(ds, sql, args, null);
    }

    public int update(DataSource ds, String sql, Object[] args, GeneratedKeyHolder holder) {
        Connection conn = JdbcUtils.getConnection(ds);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            boolean needGenerateKey = holder != null;
            ps = needGenerateKey ?
                    conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : // 生成自增key
                    conn.prepareStatement(sql); // 不生成自增key
            setValues(ps, args);
            int r = ps.executeUpdate();
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
            throw new UncheckedSQLException(e.getMessage(), e);
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            JdbcUtils.closeConnection(conn);
        }
    }

    public int[] batchUpdate(DataSource ds, String sql, List<Object[]> batchArgs) {
        Connection conn = JdbcUtils.getConnection(ds);
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(sql);
            setBatchValues(ps, batchArgs);
            return ps.executeBatch();
        } catch (SQLException e) {
            throw new UncheckedSQLException(e.getMessage(), e);
        } finally {
            JdbcUtils.closeStatement(ps);
            JdbcUtils.closeConnection(conn);
        }
    }

    private <T> T executeQuery(DataSource ds, String sql, Object[] args, ResultSetExtractor<T> rse) {
        Connection conn = JdbcUtils.getConnection(ds);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            setValues(ps, args);
            rs = ps.executeQuery();
            return rse.extractData(rs);
        } catch (SQLException e) {
            throw new UncheckedSQLException(e.getMessage(), e);
        } finally {
            JdbcUtils.closeResultSet(rs);
            JdbcUtils.closeStatement(ps);
            JdbcUtils.closeConnection(conn);
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






















