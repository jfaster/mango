package cc.concurrent.mango.util.reflect;

import cc.concurrent.mango.exception.NotReadablePropertyException;
import com.google.common.base.Strings;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public class TypeUtil {

    public static Type getPropertyType(Type type, String parameterName, String propertyPath) {
        if (Strings.isNullOrEmpty(propertyPath)) { // 如果propertyPath为空，直接返回
            return type;
        }
        int pos = propertyPath.indexOf('.');
        StringBuffer nestedPath = new StringBuffer(propertyPath.length());
        while (pos > -1) {
            String propertyName = propertyPath.substring(0, pos);
            nestedPath.append(propertyName);
            Class<?> clazz = getClassFromType(type);
            if (clazz != null) {
                Method method = MethodCache.getReadMethod(clazz, propertyName);
                if (method != null) {
                    type = method.getGenericReturnType();
                    propertyPath = propertyPath.substring(pos + 1);
                    pos = propertyPath.indexOf('.');
                    nestedPath.append(".");
                    continue;
                }
            }
            throw new NotReadablePropertyException("property ':" + parameterName + "." + nestedPath + "' is not readable");
        }

        Class<?> clazz = getClassFromType(type);
        if (clazz != null) {
            Method method = MethodCache.getReadMethod((Class<?>) type, propertyPath);
            if (method != null) {
                type = method.getGenericReturnType();
                return type;
            }
        }
        nestedPath.append(propertyPath);
        throw new NotReadablePropertyException("property ':" + parameterName + "." + nestedPath + "' is not readable");
    }

    private static Class<?> getClassFromType(Type type) {
        Class<?> clazz = null;
        if (type instanceof Class<?>) {
            clazz = (Class<?>) type;
        } else if (type instanceof ParameterizedType) {
            clazz = (Class<?>) ((ParameterizedType) type).getRawType();
        }
        return clazz;
    }

}















