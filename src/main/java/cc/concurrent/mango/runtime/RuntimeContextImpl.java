package cc.concurrent.mango.runtime;

import cc.concurrent.mango.util.reflect.BeanUtil;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;


/**
 * @author ash
 */
public class RuntimeContextImpl implements RuntimeContext {

    private final Map<String, Object> parameterMap;
    private final Map<String, Object> cache;

    public RuntimeContextImpl(Map<String, Object> parameterMap) {
        this.parameterMap = parameterMap;
        this.cache = new HashMap<String, Object>();
    }

    @Override
    public Object getPropertyValue(String parameterName, String propertyPath) {
        String key = getCacheKey(parameterName, propertyPath);
        Object cachedValue = cache.get(key);
        if (cachedValue != null) { // 非null缓存命中，直接返回
            return cachedValue;
        }
        Object object = parameterMap.get(parameterName);
        Object value = !propertyPath.isEmpty() ?
                BeanUtil.getPropertyValue(object, propertyPath, parameterName) :
                object;
        cache.put(key, value);
        return value;
    }

    @Override
    @Nullable
    public Object getNullablePropertyValue(String parameterName, String propertyPath) {
        String key = getCacheKey(parameterName, propertyPath);
        if (cache.containsKey(key)) { // 有可能缓存null对象
            return cache.get(key);
        }
        Object object = parameterMap.get(parameterName);
        Object value = !propertyPath.isEmpty() ?
                BeanUtil.getNullablePropertyValue(object, propertyPath, parameterName) :
                object;
        cache.put(key, value);
        return value;
    }

    @Override
    public void setPropertyValue(String parameterName, String propertyPath, Object value) {
        String key = getCacheKey(parameterName, propertyPath);
        cache.put(key, value);
    }

    private String getCacheKey(String parameterName, String propertyPath) {
        return propertyPath.isEmpty() ? parameterName : parameterName + "." + propertyPath;
    }

}
