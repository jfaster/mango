package cc.concurrent.mango.operator;

import cc.concurrent.mango.jdbc.core.JdbcTemplate;

import java.lang.reflect.Type;

/**
 * @author ash
 */
public abstract class AbstractOperator implements Operator {

    protected JdbcTemplate jdbcTemplate;

    protected AbstractOperator() {
        this.jdbcTemplate = new JdbcTemplate();
    }

}
