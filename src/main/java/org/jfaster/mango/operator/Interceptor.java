package org.jfaster.mango.operator;

import org.jfaster.mango.support.SqlDescriptor;

import java.util.List;

/**
 * @author ash
 */
public interface Interceptor {

    public void intercept(SqlDescriptor sqlDescriptor, List<MethodParameter> methodParameters);

}
