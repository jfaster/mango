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
        Class<T> mappedClass = rowMapper.getMappedClass();
        if (!mappedClass.isPrimitive()) {
            return rs.next() ? rowMapper.mapRow(rs, 1) : null;
        }

        // 原生类型
        if (!rs.next()) {
            throw new NullPointerException("can't cast null to primitive type " + mappedClass);
        }
        T r = rowMapper.mapRow(rs, 1);
        if (r == null) {
            throw new NullPointerException("can't cast null to primitive type " + mappedClass);
        }
        return r;
    }

}
