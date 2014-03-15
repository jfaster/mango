package cc.concurrent.mango.runtime.operator;


import cc.concurrent.mango.CacheHandler;
import cc.concurrent.mango.DataSourceFactory;

import java.lang.reflect.Type;

/**
 * @author ash
 */
public interface Operator {

    public void setDataSourceFactory(DataSourceFactory dataSourceFactory);

    public void setCacheHandler(CacheHandler cacheHandler);

    public void checkType(Type[] methodArgTypes);

    public Object execute(Object[] methodArgs);

}
