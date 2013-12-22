package cc.concurrent.mango.operator;

import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import com.google.common.base.Objects;

import javax.sql.DataSource;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author ash
 */
public class UpdateOperator extends AbstractOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(UpdateOperator.class);

    private boolean returnGeneratedId;

    protected UpdateOperator(Class<?> returnClass, boolean returnGeneratedId) {
        this.returnGeneratedId = returnGeneratedId;
        checkReturnType(returnClass);
    }

    private void checkReturnType(Class<?> returnClass) {
        if (returnGeneratedId) {
            checkState(Integer.class.equals(returnClass) || int.class.equals(returnClass),
                    "need Integer or int but " + returnClass);
        } else {
            checkState(Integer.class.equals(returnClass) || int.class.equals(returnClass) ||
                    Void.class.equals(returnClass) || void.class.equals(returnClass),
                    "need Integer or int or Void or void but " + returnClass);
        }
    }

    @Override
    public Object execute(DataSource ds, ParsedSql... parsedSqls) {
        ParsedSql parsedSql = parsedSqls[0];
        String sql = parsedSql.getSql();
        Object[] args = parsedSql.getArgs();
        if (logger.isDebugEnabled()) {
            logger.debug(Objects.toStringHelper("UpdateOperator").add("sql", sql).add("args", Arrays.toString(args)).toString());
        }
        return jdbcTemplate.update(ds, sql, args, returnGeneratedId);
    }
}
