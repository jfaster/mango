package org.jfaster.mango.invoker;

import java.lang.reflect.Type;

/**
 * @author ash
 */
public interface GetterInvokerChain {

    public Type getFinalType();

    public Object invoke(Object obj);

    public String getPropertyPath();

}
