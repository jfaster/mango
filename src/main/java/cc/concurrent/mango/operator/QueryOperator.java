package cc.concurrent.mango.operator;

import cc.concurrent.mango.logging.InternalLogger;
import cc.concurrent.mango.logging.InternalLoggerFactory;
import cc.concurrent.mango.runtime.ParsedSql;
import com.google.common.base.Objects;

import java.util.Arrays;
import java.util.List;

/**
 * @author ash
 */
public class QueryOperator implements Operator {

    private final InternalLogger logger = InternalLoggerFactory.getInstance(QueryOperator.class);

    @Override
    public Object execute(ParsedSql... parsedSqls) {
        ParsedSql parsedSql = parsedSqls[0];
        String sql = parsedSql.getSql();
        Object[] args = parsedSql.getArgs();
        if (logger.isDebugEnabled()) {
            logger.debug(Objects.toStringHelper("QueryOperator").add("sql", sql).add("args", Arrays.toString(args)).toString());
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
