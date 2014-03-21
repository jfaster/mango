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
    @Nullable
    public Object getPropertyValue(String parameterName, String propertyPath) {
        String key = getCacheKey(parameterName, propertyPath);
        if (cache.containsKey(key)) { // 有可能缓存null对象
            return cache.get(key);
        }
        Object object = parameterMap.get(parameterName);
        Object value = !propertyPath.isEmpty() ?
                BeanUtil.getPropertyValue(object, propertyPath, parameterName) :
                object;
        cache.put(key, value);
        return value;
    }

    @Override
    public void setPropertyValue(String parameterName, String propertyPath, Object value) {
        String key = getCacheKey(parameterName, propertyPath);
        cache.put(key, value);
    }

    private String getCacheKey(String beanName, String propertyPath) {
        return propertyPath.isEmpty() ? beanName : beanName + "." + propertyPath;
    }

}
