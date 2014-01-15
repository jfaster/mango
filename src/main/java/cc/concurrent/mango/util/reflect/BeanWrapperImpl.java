package cc.concurrent.mango.util.reflect;

import java.beans.IntrospectionException;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * @author ash
 */
public class BeanWrapperImpl implements BeanWrapper {

    private final Object object;

    public BeanWrapperImpl(Object object) {
        this.object = object;
    }

    @Override
    public void setPropertyValue(String propertyPath, Object value) {
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

    @Override
    public Object getPropertyValue(String propertyPath) {
        Object value = object;
        int pos = propertyPath.indexOf('.');
        while (pos > -1) {
            String propertyName = propertyPath.substring(0, pos);
            try {
                PropertyDescriptor pd = new PropertyDescriptor(propertyName, value.getClass());
                Method method = pd.getReadMethod();
                value = method.invoke(value, (Object[]) null);
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
            PropertyDescriptor pd = new PropertyDescriptor(propertyPath, value.getClass());
            Method method = pd.getReadMethod();
            value = method.invoke(value, (Object[]) null);
        } catch (IntrospectionException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (InvocationTargetException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        } catch (IllegalAccessException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
        return value;
    }

}



















