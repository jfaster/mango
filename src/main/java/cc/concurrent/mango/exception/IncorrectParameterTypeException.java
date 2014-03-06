package cc.concurrent.mango.exception;

/**
 * 错误的参数类型异常
 *
 * @author ash
 */
public class IncorrectParameterTypeException extends RuntimeException {

    public IncorrectParameterTypeException(String message) {
        super(message);
    }
}
