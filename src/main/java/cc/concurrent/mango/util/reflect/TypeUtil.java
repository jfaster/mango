package cc.concurrent.mango.util.reflect;

import com.google.common.base.Strings;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * @author ash
 */
public class TypeUtil {

    public static Class<?> getPropertyType(Class<?> clazz, String propertyPath) {
        Class<?> type = clazz;
        if (Strings.isNullOrEmpty(propertyPath)) { // 如果propertyPath为空，直接返回clazz
            return clazz;
        }
        int pos = propertyPath.indexOf('.');
        while (pos > -1) {
            String propertyName = propertyPath.substring(0, pos);
            try {
                PropertyDescriptor pd = new PropertyDescriptor(propertyName, type);
                Method method = pd.getReadMethod();
                type = method.getReturnType();
            } catch (IntrospectionException e) {
                e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
            }
            propertyPath = propertyPath.substring(pos + 1);
            pos = propertyPath.indexOf('.');
        }
        try {
            PropertyDescriptor pd = new PropertyDescriptor(propertyPath, type);
            Method method = pd.getReadMethod();
            type = method.getReturnType();
        } catch (IntrospectionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return type;
    }

}
