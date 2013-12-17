package cc.concurrent.mango.jdbc.core;

import cc.concurrent.mango.jdbc.exception.IncorrectResultSetColumnCountException;
import cc.concurrent.mango.jdbc.support.JdbcUtils;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * 单列RowMapper
 *
 * @author ash
 */
public class SingleColumnRowMapper<T> implements RowMapper<T> {

    private Class<T> mappedClass;

    public SingleColumnRowMapper(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
    }

    @SuppressWarnings("unchecked")
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        // 验证列数
        ResultSetMetaData rsmd = rs.getMetaData();
        int nrOfColumns = rsmd.getColumnCount();
        if (nrOfColumns != 1) {
            throw new IncorrectResultSetColumnCountException(1, nrOfColumns);
        }

        Object result = getColumnValue(rs, 1, this.mappedClass);
        return (T) result;
    }

    @Override
    public Class<T> getMappedClass() {
        return mappedClass;
    }

    protected Object getColumnValue(ResultSet rs, int index, Class requiredType) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index, requiredType);
    }

}
