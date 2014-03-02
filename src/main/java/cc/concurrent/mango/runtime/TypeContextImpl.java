package cc.concurrent.mango.runtime;

import cc.concurrent.mango.util.reflect.TypeUtil;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

import java.lang.reflect.Type;
import java.util.Map;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * @author ash
 */
public class TypeContextImpl implements TypeContext {

    private final Map<String, Type> parameterTypeMap;
    private final Map<String, Type> cache;

    public TypeContextImpl(Map<String, Type> parameterTypeMap) {
        this.parameterTypeMap = parameterTypeMap;
        this.cache = Maps.newHashMap();
    }

    @Override
    public Type getPropertyType(String beanName, String propertyName) {
        Type parameterType = parameterTypeMap.get(beanName);
        if (Strings.isNullOrEmpty(propertyName)) {
            return parameterType;
        }
        checkNotNull(parameterType);
        String key = getCacheKey(beanName, propertyName);
        Type type = cache.get(key);
        if (type != null) {
            return type;
        }
        type = TypeUtil.getPropertyType(parameterType, propertyName);
        if (type == null) {
            throw new RuntimeException(""); // TODO
        }
        cache.put(key, type);
        return type;
    }

    private String getCacheKey(String beanName, String propertyName) {
        return beanName + "." + propertyName;
    }

}
