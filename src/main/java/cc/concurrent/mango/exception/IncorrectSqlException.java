package cc.concurrent.mango.exception;

/**
 * 不正确的sql异常
 *
 * @author ash
 */
public class IncorrectSqlException extends RuntimeException {

    public IncorrectSqlException(String message) {
        super(message);
    }
}
