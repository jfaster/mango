package cc.concurrent.mango;

import com.google.common.reflect.AbstractInvocationHandler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author ash
 */
public class MangoInvocationHandler extends AbstractInvocationHandler implements InvocationHandler {

    private final Mango mango;

    public MangoInvocationHandler(Mango mango) {
        this.mango = mango;
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
        return mango.handleInvocation(method, args);
    }

}
