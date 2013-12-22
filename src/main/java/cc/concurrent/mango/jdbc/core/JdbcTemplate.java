package cc.concurrent.mango.jdbc.core;

import cc.concurrent.mango.jdbc.exception.DataAccessException;
import cc.concurrent.mango.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Preconditions.*;

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

    public int update(DataSource ds, String sql, Object[] args, boolean returnGenerateId) {
        Connection conn = JdbcUtils.getConnection(ds);
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = returnGenerateId ?
                    conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS) : // 生成自增id
                    conn.prepareStatement(sql); // 不生成自增id
            setValues(ps, args);
            int r = ps.executeUpdate();
            if (!returnGenerateId) { // 不生成自增id
                return r;
            }
            // 生成自增id
            rs = ps.getGeneratedKeys();
            checkState(rs.next(), "getGeneratedKeys error");
            return rs.getInt(1);
        } catch (SQLException e) {
            throw new DataAccessException(e);
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
            throw new DataAccessException(e);
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
            throw new DataAccessException(e);
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






















