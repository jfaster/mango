package org.jfaster.mango.interceptor;

/**
 * @author ash
 */
public interface Interceptor {

    public void intercept(String sql, Object[] args);

}
