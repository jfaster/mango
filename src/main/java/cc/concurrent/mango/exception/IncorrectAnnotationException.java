package cc.concurrent.mango.exception;

/**
 * CacheBy注解异常
 *
 * @author ash
 */
public class IncorrectAnnotationException extends RuntimeException {

    public IncorrectAnnotationException(String message) {
        super(message);
    }

}
