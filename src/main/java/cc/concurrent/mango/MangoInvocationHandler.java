package cc.concurrent.mango;

import cc.concurrent.mango.annotation.SQL;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.runtime.parser.ParseException;
import cc.concurrent.mango.runtime.parser.Parser;
import com.google.common.base.Strings;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.*;

/**
 * @author ash
 */
public class MangoInvocationHandler implements InvocationHandler {

    private static final Object[] NO_ARGS = {};

    private final ConcurrentHashMap<Method, ASTRootNode> nodes = new ConcurrentHashMap<Method, ASTRootNode>();

    protected Object handleInvocation(Method method, Object[] args) throws Throwable {
        ASTRootNode node = getASTRootNode(method);
        node.dump("");
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


    @Override
    public final Object invoke(Object proxy, Method method, @Nullable Object[] args) throws Throwable {
        if (args == null) {
            args = NO_ARGS;
        }
        if (args.length == 0 && method.getName().equals("hashCode")) {
            return hashCode();
        }
        if (args.length == 1
                && method.getName().equals("equals")
                && method.getParameterTypes()[0] == Object.class) {
            Object arg = args[0];
            return proxy.getClass().isInstance(arg) && equals(Proxy.getInvocationHandler(arg));
        }
        if (args.length == 0 && method.getName().equals("toString")) {
            return toString();
        }
        return handleInvocation(method, args);
    }

    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
