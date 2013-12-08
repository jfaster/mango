package cc.concurrent.mango;

import cc.concurrent.mango.annotation.SQL;
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
        checkArgument(args.length == 1, "need one and only one parameter but " + args.length);
        Object arg0 = args[0];
        checkArgument(arg0 instanceof Collection, "first parameter must be instanceof Collection");
        Collection collection = (Collection) arg0;
        checkArgument(collection.size() > 0, "first parameter can't be empty");
        ParsedSql[] parsedSqls = new ParsedSql[collection.size()];
        int index = 0;
        for (Object obj : collection) {
            Map<String, Object> parameters = Maps.newHashMap();
            parameters.put("1", obj);
            RuntimeContext context = new RuntimeContextImpl(parameters);
            ParsedSql parsedSql = node.getSqlAndArgs(context);
            parsedSqls[index++] = parsedSql;
        }
        return operator.execute(parsedSqls);
    }

    private Object handleQueryOrUpdate(ASTRootNode node, Object[] args, Operator operator) {
        Map<String, Object> parameters = Maps.newHashMap();
        for (int i = 0; i < args.length; i++) {
            parameters.put(String.valueOf(i + 1), args[i]);
        }
        RuntimeContext context = new RuntimeContextImpl(parameters);
        ParsedSql parsedSql = node.getSqlAndArgs(context);
        return operator.execute(parsedSql);
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
        operator.setDataSource(dataSource);

        return new MethodDescriptor(node, operator);
    }

}
