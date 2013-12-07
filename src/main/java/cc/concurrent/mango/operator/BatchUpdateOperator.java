package cc.concurrent.mango.operator;

import cc.concurrent.mango.logging.InternalLogger;
import cc.concurrent.mango.logging.InternalLoggerFactory;
import com.google.common.base.Joiner;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import java.util.Arrays;
import java.util.List;

/**
 * @author ash
 */
public class BatchUpdateOperator implements Operator {

    private final InternalLogger logger = InternalLoggerFactory.getInstance(BatchUpdateOperator.class);

    @Override
    public Object execute(String sql, List<Object[]> batchArgs) {
        if (logger.isDebugEnabled()) {
            List<String> str = Lists.newArrayList();
            for (Object[] args : batchArgs) {
                str.add(Arrays.toString(args));
            }
            logger.debug(Objects.toStringHelper("BatchUpdateOperator").add("sql", sql).add("batchArgs", str).toString());
        }
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

}
