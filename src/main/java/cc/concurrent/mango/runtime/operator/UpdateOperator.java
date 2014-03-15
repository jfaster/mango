package cc.concurrent.mango.runtime.operator;

import cc.concurrent.mango.ReturnGeneratedId;
import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public class UpdateOperator extends AbstractOperator {

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
        RuntimeContext context = getRuntimeContext(methodArgs);
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
        if (cacheDescriptor.isUseCache()) {
            cacheHandler.delete(getSingleKey(context));
            if (logger.isDebugEnabled()) {
                logger.debug("cache delete #key={}", getSingleKey(context));
            }
        }
        return r;
    }

}
