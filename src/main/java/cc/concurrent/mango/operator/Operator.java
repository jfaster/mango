package cc.concurrent.mango.operator;


import cc.concurrent.mango.DataCache;

import javax.sql.DataSource;

/**
 * @author ash
 */
public interface Operator {

    public Object execute(Object[] methodArgs);

    public void setDataSource(DataSource ds);

    public void setDataCache(DataCache dc);

}
