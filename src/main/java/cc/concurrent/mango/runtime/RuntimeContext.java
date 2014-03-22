package cc.concurrent.mango.runtime;

/**
 * @author ash
 */
public interface RuntimeContext {

    public Object getPropertyValue(String parameterName, String propertyPath);

    public Object getNullablePropertyValue(String parameterName, String propertyPath);

    public void setPropertyValue(String parameterName, String propertyPath, Object value);

}
