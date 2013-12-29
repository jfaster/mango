package cc.concurrent.mango.operator;

import cc.concurrent.mango.DataCache;
import cc.concurrent.mango.jdbc.JdbcTemplate;

import javax.sql.DataSource;

/**
 * @author ash
 */
public abstract class AbstractOperator implements Operator {

    protected JdbcTemplate jdbcTemplate;
    protected DataCache dataCache;

    protected AbstractOperator() {
    }

    public void setDataSource(DataSource ds) {
        this.jdbcTemplate = new JdbcTemplate(ds);
    }

    public void setDataCache(DataCache dataCache) {
        this.dataCache = dataCache;
    }

}
