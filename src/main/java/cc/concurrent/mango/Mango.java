package cc.concurrent.mango;

import cc.concurrent.mango.annotation.SQL;
import cc.concurrent.mango.exception.EmptyParameterException;
import cc.concurrent.mango.exception.NullParameterException;
import cc.concurrent.mango.operator.BatchUpdateOperator;
import cc.concurrent.mango.operator.Operator;
import cc.concurrent.mango.operator.OperatorFactory;
import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.RuntimeContextImpl;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.runtime.parser.ParseException;
import cc.concurrent.mango.runtime.parser.Parser;
import com.google.common.base.Strings;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Maps;
import com.google.common.reflect.Reflection;

import javax.sql.DataSource;
import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author ash
 */
public class Mango {

    private final DataSource dataSource;

    public Mango(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T create(Class<T> daoClass) {
        return Reflection.newProxy(daoClass, new MangoInvocationHandler(this));
    }

    protected Object handleInvocation(Method method, Object[] args) throws Throwable {
        MethodDescriptor descriptor = cache.get(method);
        ASTRootNode node = descriptor.getNode();
        Operator operator = descriptor.getOperator();

        if (operator instanceof BatchUpdateOperator) { // batchUpdate
            return handleBatchUpdate(node, args, operator);
        } else { // query or update
            return handleQueryOrUpdate(node, args, operator);
        }
    }

    private Object handleBatchUpdate(ASTRootNode node, Object[] args, Operator operator) {
        if (args.length != 1) {
            throw new IllegalStateException(); // 理论上不会抛出这个异常，如果抛出这个异常，说明程序上存在bug
        }
        Object arg0 = args[0];
        if (arg0 == null) {
            throw new NullParameterException("batchUpdate's parameter can't be null");
        }
        ParsedSql[] parsedSqls;
        if (arg0.getClass().isArray()) { // 数组
            int length = Array.getLength(arg0);
            if (length == 0) {
                throw new EmptyParameterException("array can't be empty");
            }
            parsedSqls = new ParsedSql[length];
            for (int i = 0; i < length; i++) {
                Object obj = Array.get(arg0, i);
                parsedSqls[i] = getParsedSql(node, obj);
            }
        } else if (Collection.class.isAssignableFrom(arg0.getClass())) { // 集合
            Collection<?> collection = (Collection<?>) arg0;
            if (collection.size() == 0) {
                throw new EmptyParameterException("collection can't be empty");
            }
            parsedSqls = new ParsedSql[collection.size()];
            int i = 0;
            for (Object obj : collection) {
                parsedSqls[i++] = getParsedSql(node, obj);
            }
        } else {
            throw new IllegalStateException(); // 理论上不会抛出这个异常，如果抛出这个异常，说明程序上存在bug
        }
        return operator.execute(this.dataSource, parsedSqls);
    }

    private ParsedSql getParsedSql(ASTRootNode node, Object obj) {
        Map<String, Object> parameters = Maps.newHashMap();
        parameters.put("1", obj);
        RuntimeContext context = new RuntimeContextImpl(parameters);
        return node.getSqlAndArgs(context);
    }


    private Object handleQueryOrUpdate(ASTRootNode node, Object[] args, Operator operator) {
        Map<String, Object> parameters = Maps.newHashMap();
        for (int i = 0; i < args.length; i++) {
            parameters.put(String.valueOf(i + 1), args[i]);
        }
        RuntimeContext context = new RuntimeContextImpl(parameters);
        ParsedSql parsedSql = node.getSqlAndArgs(context);
        return operator.execute(this.dataSource, parsedSql);
    }

    LoadingCache<Method, MethodDescriptor> cache = CacheBuilder.newBuilder()
            .build(
                    new CacheLoader<Method, MethodDescriptor>() {
                        public MethodDescriptor load(Method method) throws Exception{
                            return getMethodDescriptor(method);
                        }
                    });

    private MethodDescriptor getMethodDescriptor(Method method) throws ParseException {
        SQL anno = method.getAnnotation(SQL.class);
        checkArgument(anno != null, "need SQL annotation in method " + method.getName());
        String sql = anno.value();
        checkArgument(!Strings.isNullOrEmpty(sql), "sql is empty in method " + method.getName());

        ASTRootNode node = new Parser(sql).parse();
        Operator operator = OperatorFactory.getOperator(method);

        return new MethodDescriptor(node, operator);
    }

}
