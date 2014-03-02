package cc.concurrent.mango.util.reflect;

import com.google.common.base.Strings;

import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public class TypeUtil {

    public static Type getPropertyType(Type type, String propertyPath) {
        if (Strings.isNullOrEmpty(propertyPath)) { // 如果propertyPath为空，直接返回clazz
            return type;
        }
        int pos = propertyPath.indexOf('.');
        while (pos > -1) {
            String propertyName = propertyPath.substring(0, pos);
            if (type instanceof Class<?>) {
                Method method = MethodCache.getReadMethod((Class<?>) type, propertyName);
                if (method != null) {
                    type = method.getGenericReturnType();
                    propertyPath = propertyPath.substring(pos + 1);
                    pos = propertyPath.indexOf('.');
                    continue;
                }
            }
            return null;
        }
        if (type instanceof Class<?>) {
            Method method = MethodCache.getReadMethod((Class<?>) type, propertyPath);
            if (method != null) {
                type = method.getGenericReturnType();
                return type;
            }
        }
        return null;
    }

}
