package cc.concurrent.mango.operator;

import cc.concurrent.mango.exception.EmptyParameterException;
import cc.concurrent.mango.exception.NullParameterException;
import cc.concurrent.mango.exception.structure.IncorrectParameterTypeException;
import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.RuntimeContextImpl;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.Iterables;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ash
 */
public class BatchUpdateOperator extends AbstractOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(BatchUpdateOperator.class);

    private ASTRootNode rootNode;

    public BatchUpdateOperator(ASTRootNode rootNode) {
        this.rootNode = rootNode;
    }

    @Override
    public Object execute(Object[] methodArgs) {
        Object methodArg = methodArgs[0];
        if (methodArg == null) {
            throw new NullParameterException("batchUpdate's parameter can't be null");
        }
        Iterables iterables = new Iterables(methodArg);
        if (!iterables.isIterable()) {
            throw new IncorrectParameterTypeException("expected collection or array but " + methodArg.getClass());
        }
        if (iterables.isEmpty()) {
            throw new EmptyParameterException("batchUpdate's parameter can't be empty");
        }

        Set<String> keys = Sets.newHashSet();
        boolean isUseCache = cacheDescriptor.isUseCache();
        List<Object[]> batchArgs = Lists.newArrayList();
        String sql = null;
        for (Object obj : iterables) {
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("1", obj);
            RuntimeContext context = new RuntimeContextImpl(parameters);
            if (isUseCache) {
                keys.add(getKey(context));
            }
            ParsedSql parsedSql= rootNode.getSqlAndArgs(context);
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
        int[] ints = jdbcTemplate.batchUpdate(sql, batchArgs);
        if (isUseCache) {
            dataCache.delete(keys);
        }
        return ints;
    }

}
