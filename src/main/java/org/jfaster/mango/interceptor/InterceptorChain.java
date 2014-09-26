package org.jfaster.mango.interceptor;

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

    public void intercept(String sql, Object[] args) {
        if (getInterceptors() != null) {
            for (Interceptor interceptor : getInterceptors()) {
                interceptor.intercept(sql, args);
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
