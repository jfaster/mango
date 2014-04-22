package cc.concurrent.mango.exception;

/**
 * 获取自增id失败异常
 *
 * @author ash
 */
public class GeneratedKeysException extends DataAccessException {

    public GeneratedKeysException(String message) {
        super(message);
    }

}
