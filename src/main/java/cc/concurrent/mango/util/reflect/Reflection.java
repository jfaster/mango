package cc.concurrent.mango.util.reflect;


import cc.concurrent.mango.exception.reflect.BeanInstantiationException;
import cc.concurrent.mango.exception.reflect.BeansException;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;

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

    public static PropertyDescriptor[] getPropertyDescriptors(Class<?> clazz) throws BeansException {
        try {
            BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            return beanInfo.getPropertyDescriptors();
        } catch (IntrospectionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return null;
    }

}
