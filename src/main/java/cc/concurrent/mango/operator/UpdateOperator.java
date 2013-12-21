package cc.concurrent.mango.operator;

import cc.concurrent.mango.logging.InternalLogger;
import cc.concurrent.mango.logging.InternalLoggerFactory;
import cc.concurrent.mango.runtime.ParsedSql;
import com.google.common.base.Objects;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.util.Arrays;

/**
 * @author ash
 */
public class UpdateOperator extends AbstractOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(UpdateOperator.class);

    private boolean returnGeneratedId;

    protected UpdateOperator(Type returnType, boolean returnGeneratedId) {
        this.returnGeneratedId = returnGeneratedId;
        checkReturnType(returnType);
    }

    private void checkReturnType(Type returnType) {
        if (returnType instanceof Class) {
            Class<?> clazz = (Class<?>) returnType;
            if (Integer.class.equals(clazz) || int.class.equals(clazz) || void.class.equals(clazz)) {
                return;
            }
        }
        throw new IllegalStateException("update return type need Integer or int or void but " + returnType);
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
