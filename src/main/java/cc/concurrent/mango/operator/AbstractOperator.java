package cc.concurrent.mango.operator;

import cc.concurrent.mango.*;
import cc.concurrent.mango.exception.structure.CacheByAnnotationException;
import cc.concurrent.mango.jdbc.JdbcTemplate;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.runtime.TypeContextImpl;
import com.google.common.collect.Maps;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Map;

/**
 * @author ash
 */
public abstract class AbstractOperator implements Operator {

    protected JdbcTemplate jdbcTemplate;
    protected DataCache dataCache;
    protected CacheDescriptor cacheDescriptor;

    protected AbstractOperator() {
    }

    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    public void setDataCache(DataCache dataCache) {
        this.dataCache = dataCache;
    }

    protected String getSingleKey(RuntimeContext context) {
        return getKey(cacheDescriptor.getPrefix(), context.getPropertyValue(cacheDescriptor.getBeanName(),
                cacheDescriptor.getPropertyPath()));
    }

    protected String getKey(String prefix, Object keyObj) {
        return prefix + keyObj;
    }

    protected TypeContext getTypeContext(Type[] methodArgTypes) {
        Map<String, Type> parameterTypeMap = Maps.newHashMap();
        for (int i = 0; i < methodArgTypes.length; i++) {
            parameterTypeMap.put(String.valueOf(i + 1), methodArgTypes[i]);
        }
        return new TypeContextImpl(parameterTypeMap);
    }

    protected void buildCacheDescriptor(Method method) {
        Class<?> daoClass = method.getDeclaringClass();
        Cache cacheAnno = daoClass.getAnnotation(Cache.class);
        cacheDescriptor = new CacheDescriptor();
        if (cacheAnno != null) { // dao类使用cache
            CacheIgnored cacheIgnoredAnno = method.getAnnotation(CacheIgnored.class);
            if (cacheIgnoredAnno == null) { // method不禁用cache
                cacheDescriptor.setUseCache(true);
                cacheDescriptor.setPrefix(cacheAnno.prefix());
                Annotation[][] pass = method.getParameterAnnotations();
                int num = 0;
                for (int i = 0; i < pass.length; i++) {
                    Annotation[] pas = pass[i];
                    for (Annotation pa : pas) {
                        if (CacheBy.class.equals(pa.annotationType())) {
                            cacheDescriptor.setBeanName(String.valueOf(i + 1));
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
