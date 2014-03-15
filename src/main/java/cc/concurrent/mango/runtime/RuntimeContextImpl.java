package cc.concurrent.mango.runtime;

import cc.concurrent.mango.util.Strings;
import cc.concurrent.mango.util.reflect.BeanWrapper;
import cc.concurrent.mango.util.reflect.BeanWrapperImpl;

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
    public Object getPropertyValue(String beanName, String propertyName) {
        Object bean = parameterMap.get(beanName);
        if (Strings.isNullOrEmpty(propertyName)) {
            return bean;
        }
        if (bean == null) {
            throw new RuntimeException(""); // TODO
        }
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
            parameterMap.put(beanName, value);
        } else {
            String key = getCacheKey(beanName, propertyName);
            cache.put(key, value);
        }
    }

    private String getCacheKey(String beanName, String propertyName) {
        return beanName + "." + propertyName;
    }

}
