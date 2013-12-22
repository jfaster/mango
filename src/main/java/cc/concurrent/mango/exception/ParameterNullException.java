package cc.concurrent.mango.exception;

/**
 * 传入参数为null异常
 *
 * @author ash
 */
public class ParameterNullException extends RuntimeException {

    public ParameterNullException(String message) {
        super(message);
    }

}
