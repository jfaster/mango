package cc.concurrent.mango.runtime;

import cc.concurrent.mango.util.reflect.TypeUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author ash
 */
public class TypeContextImpl implements TypeContext {

    private final Map<String, Class<?>> parameterTypeMap;
    private final Map<String, Class<?>> cache;

    public TypeContextImpl(Map<String, Class<?>> parameterTypeMap) {
        this.parameterTypeMap = parameterTypeMap;
        this.cache = Maps.newHashMap();
    }

    @Override
    public Class<?> getPropertyType(String beanName, String propertyName) {
        Class<?> parameterType = parameterTypeMap.get(beanName);
        if (Strings.isNullOrEmpty(propertyName)) {
            return parameterType;
        }
        checkNotNull(parameterType);
        String key = getCacheKey(beanName, propertyName);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        Class<?> type = TypeUtil.getPropertyType(parameterType, propertyName);
        cache.put(key, type);
        return type;
    }

    private String getCacheKey(String beanName, String propertyName) {
        return beanName + "." + propertyName;
    }

}
