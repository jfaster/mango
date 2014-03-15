package cc.concurrent.mango.util.reflect;


import cc.concurrent.mango.exception.reflect.BeanInstantiationException;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

/**
 * @author ash
 */
public class Reflection {

    public static <T> T instantiate(Class<T> clazz) throws BeanInstantiationException {
        if (clazz.isInterface()) {
            throw new BeanInstantiationException(clazz, "Specified class is an interface");
        }
        try {
            return clazz.newInstance();
        } catch (InstantiationException ex) {
            throw new BeanInstantiationException(clazz, "Is it an abstract class?", ex);
        } catch (IllegalAccessException ex) {
            throw new BeanInstantiationException(clazz, "Is the constructor accessible?", ex);
        }
    }

    public static <T> T newProxy(
            Class<T> interfaceType, InvocationHandler handler) {
        // TODO 参数检测
        //checkNotNull(handler);
        //checkArgument(interfaceType.isInterface(), "%s is not an interface", interfaceType);
        Object object = Proxy.newProxyInstance(
                interfaceType.getClassLoader(),
                new Class<?>[]{interfaceType},
                handler);
        return interfaceType.cast(object);
    }


}
