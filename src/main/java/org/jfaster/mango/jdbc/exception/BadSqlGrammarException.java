package org.jfaster.mango.jdbc.exception;

/**
 * 错误的sql语法异常
 *
 * @author ash
 */
public class BadSqlGrammarException extends InvalidDataAccessResourceUsageException {

    public BadSqlGrammarException(String msg) {
        super(msg);
    }

    public BadSqlGrammarException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
