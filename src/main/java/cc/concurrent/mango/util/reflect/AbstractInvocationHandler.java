
package cc.concurrent.mango.util.reflect;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public abstract class AbstractInvocationHandler implements InvocationHandler {

    private static final Object[] NO_ARGS = {};

    @Override
    public final Object invoke(Object proxy, Method method, Object[] args)
            throws Throwable {
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
        return handleInvocation(proxy, method, args);
    }

    protected abstract Object handleInvocation(Object proxy, Method method, Object[] args)
            throws Throwable;

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
