package cc.concurrent.mango.runtime;

/**
 * @author ash
 */
public class Tuple {

    private final String sql;

    private final Object[] args;

    public Tuple(String sql, Object[] args) {
        this.sql = sql;
        this.args = args;
    }

    public String getSql() {
        return sql;
    }

    public Object[] getArgs() {
        return args;
    }
}
