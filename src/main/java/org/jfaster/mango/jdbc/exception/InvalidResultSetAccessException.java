package org.jfaster.mango.jdbc.exception;

/**
 * @author ash
 * @see BadSqlGrammarException
 * @see org.springframework.jdbc.support.rowset.SqlRowSet
 */
public class InvalidResultSetAccessException extends InvalidDataAccessResourceUsageException {

    public InvalidResultSetAccessException(String msg) {
        super(msg);
    }

    public InvalidResultSetAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
