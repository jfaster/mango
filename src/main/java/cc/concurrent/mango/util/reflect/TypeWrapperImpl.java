package cc.concurrent.mango.util.reflect;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;

/**
 * @author ash
 */
public class TypeWrapperImpl implements TypeWrapper {

    private final Class<?> clazz;

    public TypeWrapperImpl(Class<?> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Class<?> getPropertyType(String propertyPath) {
        Class<?> type = clazz;
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
