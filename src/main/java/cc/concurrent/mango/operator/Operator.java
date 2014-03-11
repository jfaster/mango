package cc.concurrent.mango.operator;


import cc.concurrent.mango.DataCache;
import cc.concurrent.mango.DataSourceFactory;

import java.lang.reflect.Type;

/**
 * @author ash
 */
public interface Operator {

    public void setDataSourceFactory(DataSourceFactory dataSourceFactory);

    public void setDataCache(DataCache dataCache);

    public void checkType(Type[] methodArgTypes);

    public Object execute(Object[] methodArgs);

}
