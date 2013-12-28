package cc.concurrent.mango.operator;


import javax.sql.DataSource;

/**
 * @author ash
 */
public interface Operator {

    public Object execute(DataSource ds, Object[] methodArgs);

}
