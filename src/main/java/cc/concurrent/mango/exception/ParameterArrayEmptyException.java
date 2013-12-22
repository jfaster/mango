package cc.concurrent.mango.exception;

/**
 * 传入参数为数组但数组为空异常
 *
 * @author ash
 */
public class ParameterArrayEmptyException extends RuntimeException {

    public ParameterArrayEmptyException(String message) {
        super(message);
    }
}
