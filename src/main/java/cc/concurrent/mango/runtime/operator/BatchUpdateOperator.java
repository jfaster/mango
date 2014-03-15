package cc.concurrent.mango.runtime.operator;

import cc.concurrent.mango.exception.EmptyParameterException;
import cc.concurrent.mango.exception.NullParameterException;
import cc.concurrent.mango.jdbc.JdbcUtils;
import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.Iterables;
import cc.concurrent.mango.util.TypeToken;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author ash
 */
public class BatchUpdateOperator extends AbstractOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(BatchUpdateOperator.class);

    private ASTRootNode rootNode;

    private BatchUpdateOperator(ASTRootNode rootNode, Method method, SQLType sqlType) {
        super(method, sqlType);
        init(rootNode, method);
    }

    public void init(ASTRootNode rootNode, Method method) {
        this.rootNode = rootNode;
        checkType(method.getGenericParameterTypes());
    }

    public static BatchUpdateOperator create(ASTRootNode rootNode, Method method, SQLType sqlType) {
        return new BatchUpdateOperator(rootNode, method, sqlType);
    }

    @Override
    public void checkType(Type[] methodArgTypes) {
        Type type = methodArgTypes[0];
        TypeToken typeToken = new TypeToken(type);
        Class<?> mappedClass = typeToken.getMappedClass();
        if (mappedClass == null) {
            throw new RuntimeException(""); // TODO
        }
        if (JdbcUtils.isSingleColumnClass(mappedClass)) {
            throw new RuntimeException(""); // TODO
        }
        if (!typeToken.isIterable()) {
            throw new RuntimeException(""); // TODO
        }
        TypeContext context = getTypeContext(new Type[] {mappedClass});
        rootNode.checkType(context);
    }

    @Override
    public Object execute(Object[] methodArgs) {
        Object methodArg = methodArgs[0];
        if (methodArg == null) {
            throw new NullParameterException("batchUpdate's parameter can't be null");
        }
        Iterables iterables = new Iterables(methodArg);
        if (iterables.isEmpty()) {
            throw new EmptyParameterException("batchUpdate's parameter can't be empty");
        }

        Set<String> keys = new HashSet<String>();
        boolean isUseCache = cacheDescriptor.isUseCache();
        List<Object[]> batchArgs = new ArrayList<Object[]>();
        String sql = null;
        for (Object obj : iterables) {
            RuntimeContext context = getRuntimeContext(new Object[] {obj});
            if (isUseCache) {
                keys.add(getSingleKey(context));
            }
            ParsedSql parsedSql= rootNode.buildSqlAndArgs(context);
            if (sql == null) {
                sql = parsedSql.getSql();
            }
            batchArgs.add(parsedSql.getArgs());
        }
        if (logger.isDebugEnabled()) {
            List<String> str = new ArrayList<String>();
            for (Object[] args : batchArgs) {
                str.add(Arrays.toString(args));
            }
            logger.debug("{} #args={}", sql, batchArgs);
        }
        int[] ints = jdbcTemplate.batchUpdate(getDataSource(), sql, batchArgs);
        if (isUseCache) {
            cacheHandler.delete(keys);
        }
        return ints;
    }

}
