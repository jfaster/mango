package cc.concurrent.mango.runtime;

/**
 * @author ash
 */
public class DbDescriptor {

    private String dataSourceName;

    private String table;

    public DbDescriptor(String dataSourceName, String table) {
        this.dataSourceName = dataSourceName;
        this.table = table;
    }

    public String getDataSourceName() {
        return dataSourceName;
    }

    public String getTable() {
        return table;
    }

}
