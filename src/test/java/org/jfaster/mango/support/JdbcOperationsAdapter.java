package org.jfaster.mango.support;

import org.jfaster.mango.jdbc.GeneratedKeyHolder;
import org.jfaster.mango.jdbc.JdbcOperations;
import org.jfaster.mango.jdbc.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class JdbcOperationsAdapter implements JdbcOperations {

    @Override
    public <T> T queryForObject(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<T> queryForList(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Set<T> queryForSet(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Object queryForArray(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(DataSource ds, String sql, Object[] args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(DataSource ds, String sql, Object[] args, GeneratedKeyHolder holder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] batchUpdate(DataSource ds, String sql, List<Object[]> batchArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] batchUpdate(DataSource ds, List<String> sqls, List<Object[]> batchArgs) {
        throw new UnsupportedOperationException();
    }

}
