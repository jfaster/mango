package cc.concurrent.mango.operator;

import cc.concurrent.mango.jdbc.core.JdbcTemplate;

import java.lang.reflect.Type;

/**
 * @author ash
 */
public abstract class AbstractOperator implements Operator {

    protected final Type returnType;

    protected JdbcTemplate jdbcTemplate;

    protected AbstractOperator(Type returnType) {
        this.returnType = returnType;
        this.jdbcTemplate = new JdbcTemplate();
    }

}
