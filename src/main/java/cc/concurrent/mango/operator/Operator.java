package cc.concurrent.mango.operator;


import cc.concurrent.mango.runtime.ParsedSql;

import javax.sql.DataSource;

/**
 * @author ash
 */
public interface Operator {

    public void setDataSource(DataSource dataSource);

    public Object execute(ParsedSql... parsedSqls);

}
