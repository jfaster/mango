package org.jfaster.mango.jdbc.exception;

/**
 * 所有暂时性数据访问异常继承此类
 *
 * @author ash
 * @see java.sql.SQLTransientException
 */
public class TransientDataAccessException extends DataAccessException {

    public TransientDataAccessException(String msg) {
        super(msg);
    }

    public TransientDataAccessException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
