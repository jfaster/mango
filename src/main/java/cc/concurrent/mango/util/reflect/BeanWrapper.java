package cc.concurrent.mango.util.reflect;


import cc.concurrent.mango.exception.reflect.BeansException;

/**
 * @author ash
 */
public interface BeanWrapper {

    void setPropertyValue(String propertyName, Object value) throws BeansException;

    public Object getPropertyValue(String propertyName) throws BeansException;

}
