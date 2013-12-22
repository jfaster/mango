package cc.concurrent.mango.exception;

/**
 * 在方法上没有SQL注解异常
 *
 * @author ash
 */
public class NoSqlAnnotationException extends RuntimeException {

    public NoSqlAnnotationException(String message) {
        super(message);
    }
}
