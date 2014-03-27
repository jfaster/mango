package cc.concurrent.mango.runtime.operator;


import cc.concurrent.mango.DataSourceFactory;

import java.lang.reflect.Type;

/**
 * @author ash
 */
public interface Operator {

    public void setDataSourceFactory(DataSourceFactory dataSourceFactory);

    public Object execute(Object[] methodArgs);

}
