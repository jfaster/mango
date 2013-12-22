package cc.concurrent.mango.exception;

/**
 * 传入参数为空（长度为0）异常
 *
 * @author ash
 */
public class EmptyParameterException extends RuntimeException {

    public EmptyParameterException(String message) {
        super(message);
    }

}
