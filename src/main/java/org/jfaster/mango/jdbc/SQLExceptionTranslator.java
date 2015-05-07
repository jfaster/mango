package org.jfaster.mango.jdbc;

import org.jfaster.mango.jdbc.exception.DataAccessException;

import java.sql.SQLException;

/**
 * @author ash
 */
public interface SQLExceptionTranslator {

    DataAccessException translate(String sql, SQLException ex);

}
