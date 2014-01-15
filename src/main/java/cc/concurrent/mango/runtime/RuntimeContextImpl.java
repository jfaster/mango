package cc.concurrent.mango.runtime;

import cc.concurrent.mango.util.reflect.BeanWrapper;
import cc.concurrent.mango.util.reflect.BeanWrapperImpl;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author ash
 */
public class RuntimeContextImpl implements RuntimeContext {

    private final Map<String, Object> parameters;
    private final Map<String, Object> cache;

    public RuntimeContextImpl(Map<String, Object> parameters) {
        this.parameters = parameters;
        this.cache = Maps.newHashMap();
    }

    @Override
    public Object getPropertyValue(String beanName, String propertyName) {
        Object bean = parameters.get(beanName);
        if (Strings.isNullOrEmpty(propertyName)) {
            return bean;
        }
        checkNotNull(bean);
        String key = getCacheKey(beanName, propertyName);
        if (cache.containsKey(key)) { // 有可能缓存null对象
            return cache.get(key);
        }
        BeanWrapper bw = new BeanWrapperImpl(bean);
        Object value = bw.getPropertyValue(propertyName);
        cache.put(key, value);
        return value;
    }

    @Override
    public void setPropertyValue(String beanName, String propertyName, Object value) {
        if (Strings.isNullOrEmpty(propertyName)) {
            parameters.put(beanName, value);
        } else {
            String key = getCacheKey(beanName, propertyName);
            cache.put(key, value);
        }
    }

    private String getCacheKey(String beanName, String propertyName) {
        return beanName + "." + propertyName;
    }

}
