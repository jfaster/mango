package cc.concurrent.mango.runtime.operator;

import cc.concurrent.mango.ReturnGeneratedId;
import cc.concurrent.mango.exception.IncorrectSqlException;
import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.runtime.parser.ASTIterableParameter;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.Iterables;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;

import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class UpdateOperator extends CacheableOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(UpdateOperator.class);

    private ASTRootNode rootNode;
    private boolean returnGeneratedId;

    private UpdateOperator(ASTRootNode rootNode, Method method, SQLType sqlType) {
        super(method, sqlType);
        init(rootNode, method, sqlType);
    }

    void init(ASTRootNode rootNode, Method method, SQLType sqlType) {
        this.rootNode = rootNode;
        ReturnGeneratedId returnGeneratedIdAnno = method.getAnnotation(ReturnGeneratedId.class);
        returnGeneratedId = returnGeneratedIdAnno != null // 要求返回自增id
                && sqlType == SQLType.INSERT; // 是插入语句

        // 检测节点type
        TypeContext context = buildTypeContext(method.getGenericParameterTypes());
        rootNode.checkType(context);

        List<ASTIterableParameter> aips = rootNode.getASTIterableParameters();
        if (isUseCache()) { // 使用cache
            if (aips.size() > 1) { // 在sql中有多个in语句
                throw new IncorrectSqlException("if use cache, sql's in clause expected less than or equal 1 but "
                        + aips.size());
            }
        }
    }

    public static UpdateOperator create(ASTRootNode rootNode, Method method, SQLType sqlType) {
        return new UpdateOperator(rootNode, method, sqlType);
    }

    @Override
    public Object execute(Object[] methodArgs) {
        RuntimeContext context = buildRuntimeContext(methodArgs);
        ParsedSql parsedSql = rootNode.buildSqlAndArgs(context);
        String sql = parsedSql.getSql();
        Object[] args = parsedSql.getArgs();
        if (logger.isDebugEnabled()) {
            logger.debug("{} #args={}", sql, args);
        }
        int r = jdbcTemplate.update(getDataSource(), sql, args, returnGeneratedId);
        if (logger.isDebugEnabled()) {
            logger.debug("{} #result={}", sql, r);
        }
        if (isUseCache()) {
            Object obj = getCacheKeyObj(context);
            Iterables iterables = new Iterables(obj);
            if (iterables.isIterable()) { // 多个key，例如：update table set name="ash" where id in (1, 2, 3);
                Set<String> keys = new HashSet<String>();
                for (Object keyObj : iterables) {
                    String key = getKey(keyObj);
                    keys.add(key);
                }
                deleteFromCache(keys);
            } else { // 单个key，例如：update table set name="ash" where id ＝ 1;
                String key = getKey(obj);
                deleteFromCache(key);
            }
            if (logger.isDebugEnabled()) {
                logger.debug("cache delete #key={}", getSingleKey(context));
            }
        }
        return r;
    }

}
