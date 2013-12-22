package cc.concurrent.mango.operator;

import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import javax.sql.DataSource;
import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;

/**
 * @author ash
 */
public class BatchUpdateOperator extends AbstractOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(BatchUpdateOperator.class);

    private boolean isIntegerArray;

    public BatchUpdateOperator(Class<?> returnClass) {
        checkReturnType(returnClass);
        this.isIntegerArray = Integer[].class.equals(returnClass);
    }

    private void checkReturnType(Class<?> returnClass) {
        checkState(Integer[].class.equals(returnClass) || int[].class.equals(returnClass) ||
                Void.class.equals(returnClass) || void.class.equals(returnClass),
                "need Integer[] or int[] or Void or void but " + returnClass);
    }

    @Override
    public Object execute(DataSource ds, ParsedSql... parsedSqls) {
        checkArgument(parsedSqls.length > 0);
        String sql = null;
        List<Object[]> batchArgs = Lists.newArrayList();
        for (ParsedSql parsedSql : parsedSqls) {
            if (sql == null) {
                sql = parsedSql.getSql();
            }
            batchArgs.add(parsedSql.getArgs());
        }
        if (logger.isDebugEnabled()) {
            List<String> str = Lists.newArrayList();
            for (Object[] args : batchArgs) {
                str.add(Arrays.toString(args));
            }
            logger.debug(Objects.toStringHelper("BatchUpdateOperator").add("sql", sql).add("batchArgs", str).toString());
        }
        int[] ints = jdbcTemplate.batchUpdate(ds, sql, batchArgs);
        if (!isIntegerArray) {
            return ints;
        }
        Object array = Array.newInstance(Integer[].class, ints.length);
        for (int i = 0; i < ints.length; i++) {
            Array.set(array, i, ints[i]);
        }
        return array;
    }

}
