package cc.concurrent.mango.runtime.operator;

import cc.concurrent.mango.Cache;
import cc.concurrent.mango.CacheBy;
import cc.concurrent.mango.CacheHandler;
import cc.concurrent.mango.CacheIgnored;
import cc.concurrent.mango.exception.structure.CacheByAnnotationException;
import cc.concurrent.mango.runtime.CacheDescriptor;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.util.reflect.Reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
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
                if (num != 1) { //TODO 合适得异常处理
                    throw new CacheByAnnotationException("need 1 but " + num);
                }
            }
        }
    }

}
