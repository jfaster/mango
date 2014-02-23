package cc.concurrent.mango.operator;


import cc.concurrent.mango.DataCache;
import cc.concurrent.mango.runtime.TypeContext;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public interface Operator {

    public void setDataSource(DataSource dataSource);

    public void setDataCache(DataCache dataCache);

    public void checkType(Type[] methodArgTypes);

    public Object execute(Object[] methodArgs);

}
