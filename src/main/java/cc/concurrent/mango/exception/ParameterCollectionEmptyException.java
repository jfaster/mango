package cc.concurrent.mango.exception;

/**
 * 传入参数为集合但集合为空异常
 *
 * @author ash
 */
public class ParameterCollectionEmptyException extends RuntimeException {

    public ParameterCollectionEmptyException(String message) {
        super(message);
    }
}
