package org.jfaster.mango.operator;

import org.jfaster.mango.parser.node.ASTRootNode;
import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.util.Iterables;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author ash
 */
public class CacheableBatchUpdateOperator extends BatchUpdateOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(CacheableBatchUpdateOperator.class);

    private CacheableOperatorDriver driver;

    protected CacheableBatchUpdateOperator(ASTRootNode rootNode, CacheableOperatorDriver driver, StatsCounter statsCounter) {
        super(rootNode, driver, statsCounter);
        this.driver = driver;
    }

    @Override
    public Object execute(Object[] methodArgs) {
        Object methodArg = methodArgs[0];
        if (methodArg == null) {
            throw new NullPointerException("batchUpdate's parameter can't be null");
        }
        Iterables iterables = new Iterables(methodArg);
        if (iterables.isEmpty()) {
            throw new IllegalArgumentException("batchUpdate's parameter can't be empty");
        }

        Set<String> keys = new HashSet<String>(iterables.size() * 2);

        Map<DataSource, Group> groupMap = new HashMap<DataSource, Group>();
        for (Object obj : iterables) {
            RuntimeContext context = driver.buildRuntimeContext(new Object[]{obj});
            keys.add(driver.getCacheKey(context));
            group(context, groupMap);
        }
        int[] ints = executeDb(groupMap);
        if (logger.isDebugEnabled()) {
            logger.debug("cache delete #keys={}", keys);
        }
        driver.deleteFromCache(keys);
        return ints;
    }

}
