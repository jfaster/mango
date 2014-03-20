package cc.concurrent.mango.util.reflect;

import cc.concurrent.mango.exception.UncheckedException;
import cc.concurrent.mango.util.Strings;

import javax.annotation.Nullable;
import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ash
 */
public class BeanUtil {

    public static void setPropertyValue(Object object, String propertyPath, Object value) {
        if (Strings.isNullOrEmpty(propertyPath)) { // 如果propertyPath为空，直接返回
            return;
        }

        Object obj = object;
        int pos = propertyPath.indexOf('.');
        while (pos > -1) {
            String propertyName = propertyPath.substring(0, pos);
            try {
                PropertyDescriptor pd = new PropertyDescriptor(propertyName, obj.getClass());
                Method method = pd.getReadMethod();
                obj = method.invoke(obj, (Object[]) null);
            } catch (IntrospectionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (InvocationTargetException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            } catch (IllegalAccessException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            propertyPath = propertyPath.substring(pos + 1);
            pos = propertyPath.indexOf('.');
        }
        try {
            PropertyDescriptor pd = new PropertyDescriptor(propertyPath, obj.getClass());
            Method method = pd.getWriteMethod();
            method.invoke(obj, value);
        } catch (IntrospectionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    public static Object getPropertyValue(@Nullable Object object, String propertyPath, Object useForException) {
        try {
            Object value = object;
            int pos = propertyPath.indexOf('.');
            StringBuffer nestedPath = new StringBuffer(propertyPath.length());
            int t = 0;
            while (pos > -1) {
                if (value == null) {
                    throw new NullPointerException(getErrorMessage(nestedPath.toString(), useForException));
                }
                String propertyName = propertyPath.substring(0, pos);
                if (t != 0) {
                    nestedPath.append(".");
                }
                nestedPath.append(propertyName);
                Method method = BeanInfoCache.getReadMethod(value.getClass(), propertyName);
                value = method.invoke(value, (Object[]) null);
                propertyPath = propertyPath.substring(pos + 1);
                pos = propertyPath.indexOf('.');
                t++;
            }
            if (value == null) {
                throw new NullPointerException(getErrorMessage(nestedPath.toString(), useForException));
            }
            PropertyDescriptor pd = new PropertyDescriptor(propertyPath, value.getClass());
            Method method = pd.getReadMethod();
            value = method.invoke(value, (Object[]) null);
            return value;
        } catch (IntrospectionException e) {
            throw new UncheckedException(e.getCause());
        } catch (InvocationTargetException e) {
            throw new UncheckedException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new UncheckedException(e.getCause());
        }
    }

    private static String getErrorMessage(String nestedPath, Object useForException) {
        if (useForException instanceof String) {
            String parameterName = (String) useForException;
            if (nestedPath.isEmpty()) {
                return "parameter ':" + parameterName + "' is null";
            } else {
                return "property ':" + parameterName + "." + nestedPath + "' is null";
            }
        } else if (useForException instanceof Class) {
            Class<?> clazz = (Class<?>) useForException;
            return "property " + nestedPath + " of " + clazz + " is null, please check return type";
        } else {
            throw new IllegalArgumentException("useForException's type expected Class or String but "
                    + useForException.getClass());
        }
    }

}










