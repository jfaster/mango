package cc.concurrent.mango.operator;

import cc.concurrent.mango.logging.InternalLogger;
import cc.concurrent.mango.logging.InternalLoggerFactory;
import com.google.common.base.Objects;

import java.util.Arrays;
import java.util.List;

/**
 * @author ash
 */
public class QueryOperator implements Operator {

    private final InternalLogger logger = InternalLoggerFactory.getInstance(QueryOperator.class);

    @Override
    public Object execute(String sql, List<Object[]> batchArgs) {
        Object[] args = batchArgs.get(0);
        if (logger.isDebugEnabled()) {
            logger.debug(Objects.toStringHelper("QueryOperator").add("sql", sql).add("args", Arrays.toString(args)).toString());
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
