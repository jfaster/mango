package cc.concurrent.mango.util.reflect;

import cc.concurrent.mango.exception.UncheckedException;

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
        try {
            Class<?> rootClass = object.getClass();
            int pos = propertyPath.indexOf('.');
            StringBuffer nestedPath = new StringBuffer(propertyPath.length());
            int t = 0;
            while (pos > -1) {
                if (object == null) {
                    throw new NullPointerException(getErrorMessage(nestedPath.toString(), rootClass));
                }
                String propertyName = propertyPath.substring(0, pos);
                if (t != 0) {
                    nestedPath.append(".");
                }
                nestedPath.append(propertyName);
                PropertyDescriptor pd = new PropertyDescriptor(propertyName, object.getClass());
                Method method = pd.getReadMethod();
                object = method.invoke(object, (Object[]) null);
                propertyPath = propertyPath.substring(pos + 1);
                pos = propertyPath.indexOf('.');
                t++;
            }
            if (object == null) {
                throw new NullPointerException(getErrorMessage(nestedPath.toString(), rootClass));
            }
            PropertyDescriptor pd = new PropertyDescriptor(propertyPath, object.getClass());
            Method method = pd.getWriteMethod();
            method.invoke(object, value);
        } catch (IntrospectionException e) {
            throw new UncheckedException(e.getCause());
        } catch (InvocationTargetException e) {
            throw new UncheckedException(e.getCause());
        } catch (IllegalAccessException e) {
            throw new UncheckedException(e.getCause());
        }
    }

    @Nullable
    public static Object getPropertyValue(@Nullable Object object, String propertyPath, Object useForException) {
        try {
            int pos = propertyPath.indexOf('.');
            StringBuffer nestedPath = new StringBuffer(propertyPath.length());
            int t = 0;
            while (pos > -1) {
                if (object == null) {
                    throw new NullPointerException(getErrorMessage(nestedPath.toString(), useForException));
                }
                String propertyName = propertyPath.substring(0, pos);
                if (t != 0) {
                    nestedPath.append(".");
                }
                nestedPath.append(propertyName);
                Method method = BeanInfoCache.getReadMethod(object.getClass(), propertyName);
                object = method.invoke(object, (Object[]) null);
                propertyPath = propertyPath.substring(pos + 1);
                pos = propertyPath.indexOf('.');
                t++;
            }
            if (object == null) {
                throw new NullPointerException(getErrorMessage(nestedPath.toString(), useForException));
            }
            PropertyDescriptor pd = new PropertyDescriptor(propertyPath, object.getClass());
            Method method = pd.getReadMethod();
            object = method.invoke(object, (Object[]) null);
            return object;
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










