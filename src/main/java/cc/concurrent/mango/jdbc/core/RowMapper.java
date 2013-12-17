package cc.concurrent.mango.jdbc.core;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ash
 */
public interface RowMapper<T> {

    T mapRow(ResultSet rs, int rowNum) throws SQLException;

    Class<T> getMappedClass();

}
