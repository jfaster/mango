package cc.concurrent.mango.runtime;

/**
 * @author ash
 */
public class ParsedSql {

    private String sql;

    private Object[] args;

    public ParsedSql(String sql, Object[] args) {
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
