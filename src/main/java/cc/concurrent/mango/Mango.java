package cc.concurrent.mango;

import org.springframework.util.ClassUtils;

import java.lang.reflect.Proxy;

/**
 * @author ash
 */
public class Mango {

    @SuppressWarnings("unchecked")
    public <T> T create(Class<T> daoClass) {
        try {
            MangoInvocationHandler handler = new MangoInvocationHandler();
            ClassLoader classLoader = ClassUtils.getDefaultClassLoader();
            return (T) Proxy.newProxyInstance(classLoader, new Class[]{daoClass}, handler);
        } catch (RuntimeException e) {
            throw new IllegalStateException("failed to create bean for " + daoClass.getName(), e);
        }
    }

}
