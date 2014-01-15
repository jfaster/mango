package cc.concurrent.mango.runtime;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author ash
 */
public class TypeContextImpl implements TypeContext {

    private final Map<String, Class<?>> types;
    private final Map<String, Class<?>> cache;

    public TypeContextImpl(Map<String, Class<?>> types) {
        this.types = types;
        this.cache = Maps.newHashMap();
    }

    @Override
    public Class<?> getPropertyType(String beanName, String propertyName) {
        Class<?> type = types.get(beanName);
        if (Strings.isNullOrEmpty(propertyName)) {
            return type;
        }
        checkNotNull(type);
        String key = getCacheKey(beanName, propertyName);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }

//        BeanWrapper bw = new BeanWrapperImpl(bean);
//        Object value = bw.getPropertyValue(propertyName);
//        cache.put(key, value);
//        return value;
        return null;
    }

    private String getCacheKey(String beanName, String propertyName) {
        return beanName + "." + propertyName;
    }

}
