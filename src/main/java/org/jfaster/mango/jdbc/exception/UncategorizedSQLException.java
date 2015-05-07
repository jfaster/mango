package org.jfaster.mango.jdbc.exception;

import java.sql.SQLException;

/**
 * @author ash
 */
public class UncategorizedSQLException extends NonTransientDataAccessException {

    private final String sql;

    public UncategorizedSQLException(String sql, SQLException ex) {
        super("uncategorized SQLException for SQL [" + sql + "]; SQL state [" +
                ex.getSQLState() + "]; error code [" + ex.getErrorCode() + "]; " + ex.getMessage(), ex);
        this.sql = sql;
    }

    public SQLException getSQLException() {
        return (SQLException) getCause();
    }

    public String getSql() {
        return this.sql;
    }

}
