package cc.concurrent.mango.runtime;

import cc.concurrent.mango.exception.NotReadableParameterException;
import cc.concurrent.mango.util.reflect.TypeUtil;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ash
 */
public class TypeContextImpl implements TypeContext {

    private final Map<String, Type> parameterTypeMap;
    private final Map<String, Type> cache;

    public TypeContextImpl(Map<String, Type> parameterTypeMap) {
        this.parameterTypeMap = parameterTypeMap;
        this.cache = new HashMap<String, Type>();
    }

    @Override
    public Type getPropertyType(String parameterName, String propertyPath) {
        Type parameterType = parameterTypeMap.get(parameterName);
        if (parameterType == null ) {
            throw new NotReadableParameterException("parameter ':" + parameterName + "' is not readable");
        }
        String key = getCacheKey(parameterName, propertyPath);
        Type type = cache.get(key);
        if (type != null) {
            return type;
        }
        type = TypeUtil.getPropertyType(parameterType, parameterName, propertyPath);
        cache.put(key, type);
        return type;
    }

    private String getCacheKey(String beanName, String propertyName) {
        return beanName + "." + propertyName;
    }

}
