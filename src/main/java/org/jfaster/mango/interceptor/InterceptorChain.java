package org.jfaster.mango.interceptor;

import org.jfaster.mango.support.SqlDescriptor;

import java.util.LinkedList;
import java.util.List;

/**
 * @author ash
 */
public class InterceptorChain {

    private List<Interceptor> interceptors;

    public void addInterceptor(Interceptor interceptor) {
        initInterceptorList();
        interceptors.add(interceptor);
    }

    public void intercept(SqlDescriptor sqlDescriptor, List<MethodParameter> methodParameters) {
        if (getInterceptors() != null) {
            for (Interceptor interceptor : getInterceptors()) {
                interceptor.intercept(sqlDescriptor, methodParameters);
            }
        }
    }

    public List<Interceptor> getInterceptors() {
        return interceptors;
    }

    private void initInterceptorList() {
        if (interceptors == null) {
            interceptors = new LinkedList<Interceptor>();
        }
    }

}
