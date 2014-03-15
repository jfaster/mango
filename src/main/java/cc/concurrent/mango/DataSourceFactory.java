package cc.concurrent.mango;

import cc.concurrent.mango.runtime.operator.SQLType;

import javax.sql.DataSource;

/**
 * @author ash
 */
public interface DataSourceFactory {

    public DataSource getDataSource(String name, SQLType sqlType);

}
