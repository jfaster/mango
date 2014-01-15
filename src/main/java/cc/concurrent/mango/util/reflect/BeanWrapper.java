package cc.concurrent.mango.util.reflect;


/**
 * @author ash
 */
public interface BeanWrapper {

    void setPropertyValue(String propertyName, Object value);

    public Object getPropertyValue(String propertyName);

}
