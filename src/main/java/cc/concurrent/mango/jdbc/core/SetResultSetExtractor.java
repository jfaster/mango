package cc.concurrent.mango.jdbc.core;

import com.google.common.collect.Sets;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Set;

/**
 * @author ash
 */
public class SetResultSetExtractor<T> implements ResultSetExtractor<Set<T>> {

    private final RowMapper<T> rowMapper;

    public SetResultSetExtractor(RowMapper<T> rowMapper) {
        this.rowMapper = rowMapper;
    }

    @Override
    public Set<T> extractData(ResultSet rs) throws SQLException {
        Set<T> results = Sets.newHashSet();
        int rowNum = 0;
        while (rs.next()) {
            results.add(rowMapper.mapRow(rs, rowNum++));
        }
        return results;
    }

}
