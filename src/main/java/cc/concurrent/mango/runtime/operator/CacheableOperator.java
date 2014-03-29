package cc.concurrent.mango.runtime.operator;

import cc.concurrent.mango.Cache;
import cc.concurrent.mango.CacheBy;
import cc.concurrent.mango.CacheHandler;
import cc.concurrent.mango.CacheIgnored;
import cc.concurrent.mango.exception.IncorrectAnnotationException;
import cc.concurrent.mango.exception.IncorrectParameterTypeException;
import cc.concurrent.mango.jdbc.JdbcUtils;
import cc.concurrent.mango.runtime.CacheDescriptor;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.util.TypeToken;
import cc.concurrent.mango.util.reflect.Reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * @author ash
 */
public abstract class CacheableOperator extends AbstractOperator implements Cacheable {

    private CacheHandler cacheHandler;
    private CacheDescriptor cacheDescriptor;

    protected CacheableOperator(Method method, SQLType sqlType) {
        super(method, sqlType);
        buildCacheDescriptor(method);
    }

    @Override
    public void setCacheHandler(CacheHandler cacheHandler) {
        this.cacheHandler = cacheHandler;
    }

    protected void checkCacheType(TypeContext context) {
        if (isUseCache()) {
            String parameterName = cacheDescriptor.getParameterName();
            String propertyPath = cacheDescriptor.getPropertyPath();
            Type type = context.getPropertyType(parameterName, propertyPath);
            TypeToken typeToken = new TypeToken(type);
            Class<?> mappedClass = typeToken.getMappedClass();
            if (mappedClass == null || typeToken.isIterable() || !JdbcUtils.isSingleColumnClass(mappedClass)) {
                throw new IncorrectParameterTypeException("invalid type of " + getFullName(parameterName, propertyPath) +
                        ", need a single column class but " + type);
            }
        }
    }

    protected boolean isUseCache() {
        return cacheDescriptor.isUseCache();
    }

    protected void setToCache(String key, Object value) {
        cacheHandler.set(key, value, cacheDescriptor.getExpires());
    }

    protected void deleteFromCache(String key) {
        cacheHandler.delete(key);
    }

    protected void deleteFromCache(Set<String> keys) {
        cacheHandler.delete(keys);
    }

    protected Object getFromCache(String key) {
        return cacheHandler.get(key);
    }

    public Map<String, Object> getBulkFromCache(Set<String> keys) {
        return cacheHandler.getBulk(keys);
    }

    protected Object getCacheKeyObj(RuntimeContext context) {
        return context.getPropertyValue(cacheDescriptor.getParameterName(), cacheDescriptor.getPropertyPath());
    }

    protected String getSingleKey(RuntimeContext context) {
        return getKey(getCacheKeyObj(context));
    }

    protected String getKey(Object keyObj) {
        return cacheDescriptor.getPrefix() + keyObj;
    }

    protected String getCacheParameterName() {
        return cacheDescriptor.getParameterName();
    }

    protected String getCachePropertyPath() {
        return cacheDescriptor.getPropertyPath();
    }

    private void buildCacheDescriptor(Method method) {
        Class<?> daoClass = method.getDeclaringClass();
        Cache cacheAnno = daoClass.getAnnotation(Cache.class);
        cacheDescriptor = new CacheDescriptor();
        if (cacheAnno != null) { // dao类使用cache
            CacheIgnored cacheIgnoredAnno = method.getAnnotation(CacheIgnored.class);
            if (cacheIgnoredAnno == null) { // method不禁用cache
                cacheDescriptor.setUseCache(true);
                cacheDescriptor.setPrefix(cacheAnno.prefix());
                cacheDescriptor.setExpire(Reflection.instantiate(cacheAnno.expire()));
                cacheDescriptor.setNum(cacheAnno.num());
                Annotation[][] pass = method.getParameterAnnotations();
                int num = 0;
                for (int i = 0; i < pass.length; i++) {
                    Annotation[] pas = pass[i];
                    for (Annotation pa : pas) {
                        if (CacheBy.class.equals(pa.annotationType())) {
                            cacheDescriptor.setParameterName(getParameterNameByIndex(i));
                            cacheDescriptor.setPropertyPath(((CacheBy) pa).value());
                            num++;
                        }
                    }
                }
                if (num != 1) {
                    throw new IncorrectAnnotationException("if use cache, each method " +
                            "expected one and only one cc.concurrent.mango.CacheBy annotation but found " + num);
                }
            }
        }
    }

    private String getFullName(String parameterName, String propertyPath) {
        return !propertyPath.isEmpty() ? parameterName + "." + propertyPath : propertyPath;
    }

}
