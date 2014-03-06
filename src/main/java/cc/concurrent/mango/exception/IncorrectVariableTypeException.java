package cc.concurrent.mango.exception;

/**
 * 错误的变量类型异常
 *
 * @author ash
 */
public class IncorrectVariableTypeException extends RuntimeException {

    public IncorrectVariableTypeException(String message) {
        super(message);
    }
}
