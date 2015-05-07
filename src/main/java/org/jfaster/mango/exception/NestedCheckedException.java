package org.jfaster.mango.exception;

import org.jfaster.mango.util.NestedExceptionUtils;

/**
 * @author ash
 */
public abstract class NestedCheckedException extends Exception {

    public NestedCheckedException(String message) {
        super(message);
    }

    public NestedCheckedException(String message, Throwable cause) {
        super(message, cause);
    }

    @Override
    public String getMessage() {
        return NestedExceptionUtils.buildMessage(super.getMessage(), getCause());
    }

}
