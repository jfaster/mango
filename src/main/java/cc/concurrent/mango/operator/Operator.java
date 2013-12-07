package cc.concurrent.mango.operator;

import java.util.List;

/**
 * @author ash
 */
public interface Operator {

    public Object execute(String sql, List<Object[]> batchArgs);

}
