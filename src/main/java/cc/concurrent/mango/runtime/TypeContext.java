package cc.concurrent.mango.runtime;

/**
 * @author ash
 */
public interface TypeContext {

    public Class<?> getPropertyType(String beanName, String propertyName);

}
