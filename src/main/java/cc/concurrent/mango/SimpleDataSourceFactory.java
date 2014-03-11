package cc.concurrent.mango;

import cc.concurrent.mango.operator.SQLType;

import javax.sql.DataSource;

/**
 * @author ash
 */
public class SimpleDataSourceFactory implements DataSourceFactory {

    private final DataSource dataSource;

    public SimpleDataSourceFactory(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public DataSource getDataSource(String name, SQLType sqlType) {
        return dataSource;
    }

}
