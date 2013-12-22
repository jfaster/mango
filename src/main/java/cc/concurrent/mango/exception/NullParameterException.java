package cc.concurrent.mango.exception;

/**
 * 传入参数为null异常
 *
 * @author ash
 */
public class NullParameterException extends RuntimeException {

    public NullParameterException(String message) {
        super(message);
    }

}
