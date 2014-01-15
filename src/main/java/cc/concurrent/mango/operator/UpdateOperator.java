package cc.concurrent.mango.operator;

import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.RuntimeContextImpl;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Map;

/**
 * @author ash
 */
public class UpdateOperator extends AbstractOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(UpdateOperator.class);

    private ASTRootNode rootNode;
    private boolean returnGeneratedId;

    public UpdateOperator(ASTRootNode rootNode, boolean returnGeneratedId) {
        this.rootNode = rootNode;
        this.returnGeneratedId = returnGeneratedId;
    }

    @Override
    public Object execute(Object[] methodArgs) {
        Map<String, Object> parameters = Maps.newHashMap();
        for (int i = 0; i < methodArgs.length; i++) {
            parameters.put(String.valueOf(i + 1), methodArgs[i]);
        }
        RuntimeContext context = new RuntimeContextImpl(parameters);
        ParsedSql parsedSql = rootNode.buildSqlAndArgs(context);
        String sql = parsedSql.getSql();
        Object[] args = parsedSql.getArgs();
        if (logger.isDebugEnabled()) {
            logger.debug("{} #args={}", sql, args);
        }
        int r = jdbcTemplate.update(sql, args, returnGeneratedId);
        if (logger.isDebugEnabled()) {
            logger.debug("{} #result={}", sql, r);
        }
        if (cacheDescriptor.isUseCache()) {
            dataCache.delete(Sets.newHashSet(getSingleKey(context)));
            if (logger.isDebugEnabled()) {
                logger.debug("cache delete #key={}", getSingleKey(context));
            }
        }
        return r;
    }

}
