package cc.concurrent.mango;

import cc.concurrent.mango.operator.SQLType;

import javax.sql.DataSource;
import java.util.List;
import java.util.Random;

/**
 * @author ash
 */
public class MasterSlaveDataSourceFactory implements DataSourceFactory {

    private final DataSource master;
    private final List<DataSource> slaves;
    private final Random random = new Random();

    public MasterSlaveDataSourceFactory(DataSource master, List<DataSource> slaves) {
        this.master = master;
        this.slaves = slaves;
    }

    @Override
    public DataSource getDataSource(String name, SQLType sqlType) {
        return sqlType == SQLType.SELECT ? slaves.get(random.nextInt(slaves.size())) : master;
    }

}
