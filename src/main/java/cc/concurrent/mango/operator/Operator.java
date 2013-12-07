package cc.concurrent.mango.operator;


import cc.concurrent.mango.runtime.ParsedSql;

/**
 * @author ash
 */
public interface Operator {

    public Object execute(ParsedSql... parsedSqls);

}
