package cc.concurrent.mango.operator;

import cc.concurrent.mango.ReturnGeneratedId;
import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.RuntimeContextImpl;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author ash
 */
public class UpdateOperator extends AbstractOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(UpdateOperator.class);

    private ASTRootNode rootNode;
    private boolean returnGeneratedId;

    private UpdateOperator(ASTRootNode rootNode, Method method, SQLType sqlType) {
        this.rootNode = rootNode;
        buildCacheDescriptor(method);
        init(method, sqlType);
    }

    void init(Method method, SQLType sqlType) {
        ReturnGeneratedId returnGeneratedIdAnno = method.getAnnotation(ReturnGeneratedId.class);
        returnGeneratedId = returnGeneratedIdAnno != null // 要求返回自增id
                && sqlType == SQLType.INSERT; // 是插入语句

        checkType(method.getGenericParameterTypes());
    }

    public static UpdateOperator create(ASTRootNode rootNode, Method method, SQLType sqlType) {
        return new UpdateOperator(rootNode, method, sqlType);
    }

    @Override
    public void checkType(Type[] methodArgTypes) {
        // 检测节点type
        TypeContext context = getTypeContext(methodArgTypes);
        rootNode.checkType(context);
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
