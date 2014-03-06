package cc.concurrent.mango.exception;

/**
 * 没有可读属性异常
 *
 * @author ash
 */
public class NotReadablePropertyException extends RuntimeException {

    public NotReadablePropertyException(String message) {
        super(message);
    }
}
