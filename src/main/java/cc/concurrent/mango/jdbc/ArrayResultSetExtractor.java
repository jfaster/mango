package cc.concurrent.mango.jdbc;

import java.lang.reflect.Array;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class ArrayResultSetExtractor<T> implements ResultSetExtractor<Object> {

    private final RowMapper<T> rowMapper;

    public ArrayResultSetExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }


    @Override
    public Object extractData(ResultSet rs) throws SQLException {
        List<T> lists = new ArrayList<T>();
        int rowNum = 0;
        while (rs.next()) {
            lists.add(rowMapper.mapRow(rs, rowNum++));
        }
        Object array = Array.newInstance(rowMapper.getMappedClass(), lists.size());
        for (int i = 0; i < lists.size(); i++) {
            Array.set(array, i, lists.get(i));
        }
        return array;
    }

}
