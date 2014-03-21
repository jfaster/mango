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
        String key = getCacheKey(parameterName, propertyPath);
        Type cachedType = cache.get(key);
        if (cachedType != null) { // 缓存命中，直接返回
            return cachedType;
        }
        Type parameterType = parameterTypeMap.get(parameterName);
        if (parameterType == null ) {
            throw new NotReadableParameterException("parameter ':" + parameterName + "' is not readable");
        }
        Type type = !propertyPath.isEmpty() ?
                TypeUtil.getPropertyType(parameterType, parameterName, propertyPath) :
                parameterType;
        cache.put(key, type);
        return type;
    }

    private String getCacheKey(String parameterName, String propertyPath) {
        return propertyPath.isEmpty() ? parameterName : parameterName + "." + propertyPath;
    }

}
