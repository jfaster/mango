package cc.concurrent.mango.runtime;

/**
 * @author ash
 */
public interface RuntimeContext {

    public Object getPropertyValue(String beanName, String propertyName);

}
