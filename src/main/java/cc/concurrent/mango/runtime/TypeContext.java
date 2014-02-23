package cc.concurrent.mango.runtime;

import java.lang.reflect.Type;

/**
 * @author ash
 */
public interface TypeContext {

    public Type getPropertyType(String beanName, String propertyName);

}
