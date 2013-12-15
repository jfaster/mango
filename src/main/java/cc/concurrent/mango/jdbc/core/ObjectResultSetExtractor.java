package cc.concurrent.mango.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ash
 */
public class ObjectResultSetExtractor<T> implements ResultSetExtractor<T> {

    private final RowMapper<T> rowMapper;

    public ObjectResultSetExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public T extractData(ResultSet rs) throws SQLException {
        return rs.next() ? rowMapper.mapRow(rs, 1) : null;
    }

}
