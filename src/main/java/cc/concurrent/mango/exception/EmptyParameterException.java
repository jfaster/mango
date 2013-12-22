package cc.concurrent.mango.exception;

/**
 * 传入参数为null异常
 *
 * @author ash
 */
public class EmptyParameterException extends RuntimeException {

    public EmptyParameterException(String message) {
        super(message);
    }

}
