package cc.concurrent.mango;

import cc.concurrent.mango.runtime.operator.SQLType;

import javax.sql.DataSource;
import java.util.Map;

/**
 * @author ash
 */
public class MultipleDataSourceFactory implements DataSourceFactory {

    private final Map<String, DataSourceFactory> factories;

    public MultipleDataSourceFactory(Map<String, DataSourceFactory> factories) {
        this.factories = factories;
    }

    @Override
    public DataSource getDataSource(String name, SQLType sqlType) {
        DataSourceFactory factory = factories.get(name);
        return factory.getDataSource(name, sqlType);
    }

}
