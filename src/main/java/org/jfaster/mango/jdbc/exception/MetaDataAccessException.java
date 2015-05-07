package org.jfaster.mango.jdbc.exception;

import org.jfaster.mango.exception.NestedCheckedException;

/**
 * @author ash
 */
public class MetaDataAccessException extends NestedCheckedException {

    public MetaDataAccessException(String message) {
        super(message);
    }

    public MetaDataAccessException(String message, Throwable cause) {
        super(message, cause);
    }

}
