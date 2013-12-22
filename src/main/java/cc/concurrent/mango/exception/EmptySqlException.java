package cc.concurrent.mango.exception;

/**
 * sql为null或空异常
 *
 * @author ash
 */
public class EmptySqlException extends RuntimeException {

    public EmptySqlException(String message) {
        super(message);
    }
}
