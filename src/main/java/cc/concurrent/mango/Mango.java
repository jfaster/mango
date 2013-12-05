package cc.concurrent.mango;

import cc.concurrent.mango.annotation.SQL;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.RuntimeContextImpl;
import cc.concurrent.mango.runtime.Tuple;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.runtime.parser.ParseException;
import cc.concurrent.mango.runtime.parser.Parser;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.reflect.Reflection;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkArgument;

/**
 * @author ash
 */
public class Mango {

    private final ConcurrentHashMap<Method, ASTRootNode> nodes = new ConcurrentHashMap<Method, ASTRootNode>();

    public <T> T create(Class<T> daoClass) {
        return Reflection.newProxy(daoClass, new MangoInvocationHandler(this));
    }

    protected Object handleInvocation(Method method, Object[] args) throws Throwable {
        ASTRootNode node = getASTRootNode(method);
        Map<String, Object> parameters = Maps.newHashMap();
        for (int i = 0; i < args.length; i++) {
            parameters.put(String.valueOf(i + 1), args[i]);
        }
        RuntimeContext context = new RuntimeContextImpl(parameters);
        Tuple tuple = node.getSqlAndArgs(context);
        System.out.println(tuple.getSql());
        System.out.println(Arrays.toString(tuple.getArgs()));
        return null;
    }

    private ASTRootNode getASTRootNode(Method method) throws ParseException {
        ASTRootNode node = nodes.get(method);
        if (node == null) {
            synchronized (method) {
                node = nodes.get(method);
                if (node == null) {
                    SQL anno = method.getAnnotation(SQL.class);
                    checkArgument(anno != null, "need SQL annotation in method " + method.getName());
                    String sql = anno.value();
                    checkArgument(!Strings.isNullOrEmpty(sql), "sql is empty in method " + method.getName());
                    node = new Parser(sql).parse();
                    nodes.put(method, node);
                }
            }
        }
        return node;
    }

}
