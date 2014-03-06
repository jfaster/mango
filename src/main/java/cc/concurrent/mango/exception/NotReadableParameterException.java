package cc.concurrent.mango.exception;

/**
 * 没有可读参数异常
 *
 * @author ash
 */
public class NotReadableParameterException extends RuntimeException {

    public NotReadableParameterException(String message) {
        super(message);
    }
}
