package org.jfaster.mango.operator;

import org.jfaster.mango.exception.IncorrectSqlException;
import org.jfaster.mango.parser.node.ASTJDBCIterableParameter;
import org.jfaster.mango.parser.node.ASTRootNode;
import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.support.SQLType;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class CacheableUpdateOperator extends UpdateOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(CacheableUpdateOperator.class);

    private CacheableOperatorDriver driver;

    protected CacheableUpdateOperator(
            ASTRootNode rootNode,
            CacheableOperatorDriver driver,
            Method method,
            SQLType sqlType,
            StatsCounter statsCounter
            ) {
        super(rootNode, driver, method, sqlType, statsCounter);

        this.driver = driver;

        List<ASTJDBCIterableParameter> jips = rootNode.getJDBCIterableParameters();
        if (jips.size() > 1) {
            throw new IncorrectSqlException("if use cache, sql's in clause expected less than or equal 1 but " +
                    jips.size()); // sql中不能有多个in语句
        }
    }

    @Override
    public Object execute(Object[] methodArgs) {
        RuntimeContext context = driver.buildRuntimeContext(methodArgs);
        if (driver.isUseMultipleKeys()) { // 多个key，例如：update table set name='ash' where id in (1, 2, 3);
            Set<String> keys = driver.getCacheKeys(context);
            if (logger.isDebugEnabled()) {
                logger.debug("cache delete #keys={}", keys);
            }
            driver.deleteFromCache(keys);
        } else { // 单个key，例如：update table set name='ash' where id ＝ 1;
            String key = driver.getCacheKey(context);
            if (logger.isDebugEnabled()) {
                logger.debug("cache delete #key={}", key);
            }
            driver.deleteFromCache(key);
        }
        return execute(context);
    }

}
