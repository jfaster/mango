package org.jfaster.mango.operator;

/**
 * @author ash
 */
public interface RuntimeContextFactory {

    public RuntimeContext newRuntimeContext(Object[] values);

}
