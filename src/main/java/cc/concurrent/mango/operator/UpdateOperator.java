package cc.concurrent.mango.operator;

import cc.concurrent.mango.logging.InternalLogger;
import cc.concurrent.mango.logging.InternalLoggerFactory;
import cc.concurrent.mango.runtime.ParsedSql;
import com.google.common.base.Objects;
import com.google.common.reflect.TypeToken;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * @author ash
 */
public class UpdateOperator extends AbstractOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(UpdateOperator.class);

    protected UpdateOperator(Type returnType) {
        super(returnType);
    }

    @Override
    public Object execute(DataSource ds, ParsedSql... parsedSqls) {
        ParsedSql parsedSql = parsedSqls[0];
        String sql = parsedSql.getSql();
        Object[] args = parsedSql.getArgs();
        if (logger.isDebugEnabled()) {
            logger.debug(Objects.toStringHelper("UpdateOperator").add("sql", sql).add("args", Arrays.toString(args)).toString());
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
