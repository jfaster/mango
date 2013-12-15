package cc.concurrent.mango.jdbc.core;

import cc.concurrent.mango.jdbc.exception.DataAccessException;
import cc.concurrent.mango.jdbc.support.JdbcUtils;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class JdbcTemplate {


    public <T> T queryForObject(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        return execute(ds, sql, args, new ObjectResultSetExtractor<T>(rowMapper));
    }

    public <T> List<T> queryForList(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        return null;
    }

    public <T> Set<T> queryForSet(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        return null;
    }

    public <T> T[] queryForArray(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        return null;
    }


    private <T> T execute(DataSource ds, String sql, Object[] args, ResultSetExtractor<T> rse) {
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
        int t = 0;
        for (Object arg : args) {
            ps.setObject(++t, arg);
        }
    }

}






















