package org.jfaster.mango.jdbc.exception;

/**
 * 访问数据错误异常继承此类
 *
 * @author ash
 */
public class InvalidDataAccessResourceUsageException extends NonTransientDataAccessException {

  public InvalidDataAccessResourceUsageException(String msg) {
    super(msg);
  }

  public InvalidDataAccessResourceUsageException(String msg, Throwable cause) {
    super(msg, cause);
  }

}
