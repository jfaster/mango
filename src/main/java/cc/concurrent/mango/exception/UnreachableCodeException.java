package cc.concurrent.mango.exception;

/**
 * @author ash
 */
public class UnreachableCodeException extends RuntimeException {

    public UnreachableCodeException() {
        super("code is unreachable, if this exception is thrown, indicating a bug");
    }
}
