package cc.concurrent.mango.operator;

import cc.concurrent.mango.CacheDescriptor;
import cc.concurrent.mango.DataCache;
import cc.concurrent.mango.jdbc.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author ash
 */
public abstract class AbstractOperator implements Operator {

    protected JdbcTemplate jdbcTemplate;
    protected DataCache dataCache;
    protected CacheDescriptor cacheDescriptor;

    protected AbstractOperator() {
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setCacheDescriptor(CacheDescriptor cacheDescriptor) {
        this.cacheDescriptor = cacheDescriptor;
    }

    public void setDataCache(DataCache dataCache) {
        this.dataCache = dataCache;
    }

}
