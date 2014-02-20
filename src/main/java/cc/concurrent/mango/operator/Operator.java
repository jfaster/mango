package cc.concurrent.mango.operator;


import cc.concurrent.mango.DataCache;
import cc.concurrent.mango.runtime.TypeContext;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/**
 * @author ash
 */
public interface Operator {

    public void setDataSource(DataSource dataSource);

    public void setDataCache(DataCache dataCache);

    public void checkType(Class<?>[] methodArgTypes);

    public Object execute(Object[] methodArgs);

}
