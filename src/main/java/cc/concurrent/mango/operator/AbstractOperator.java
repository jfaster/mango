package cc.concurrent.mango.operator;

import cc.concurrent.mango.*;
import cc.concurrent.mango.exception.structure.CacheByAnnotationException;
import cc.concurrent.mango.jdbc.JdbcTemplate;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.RuntimeContextImpl;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.runtime.TypeContextImpl;
import cc.concurrent.mango.util.reflect.Reflection;
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

    private DataSourceFactory dataSourceFactory;
    private DbDescriptor dbDescriptor;
    private SQLType sqlType;
    private String[] aliases;

    protected AbstractOperator(Method method, SQLType sqlType) {
        this.jdbcTemplate = new JdbcTemplate();
        this.sqlType = sqlType;
        buildAliases(method);
        buildCacheDescriptor(method);
    }

    @Override
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public void setDataCache(DataCache dataCache) {
        this.dataCache = dataCache;
    }

    @Override
    public void setDbDescriptor(DbDescriptor dbDescriptor) {
        this.dbDescriptor = dbDescriptor;
    }

    protected String getSingleKey(RuntimeContext context) {
        return getKey(cacheDescriptor.getPrefix(), context.getPropertyValue(cacheDescriptor.getParameterName(),
                cacheDescriptor.getPropertyPath()));
    }

    protected String getKey(String prefix, Object keyObj) {
        return prefix + keyObj;
    }

    protected TypeContext getTypeContext(Type[] methodArgTypes) {
        Map<String, Type> parameterTypeMap = Maps.newHashMap();
        for (int i = 0; i < methodArgTypes.length; i++) {
            parameterTypeMap.put(getParameterNameByIndex(i), methodArgTypes[i]);
        }
        return new TypeContextImpl(parameterTypeMap);
    }

    protected RuntimeContext getRuntimeContext(Object[] methodArgs) {
        Map<String, Object> parameters = Maps.newHashMap();
        for (int i = 0; i < methodArgs.length; i++) {
            parameters.put(getParameterNameByIndex(i), methodArgs[i]);
        }
        return new RuntimeContextImpl(parameters);
    }


    protected DataSource getDataSource() {
        return dataSourceFactory.getDataSource(dbDescriptor.getDataSourceName(), sqlType);
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

    private void buildAliases(Method method) {
        Annotation[][] pass = method.getParameterAnnotations();
        aliases = new String[pass.length];
        for (int i = 0; i < pass.length; i++) {
            Annotation[] pas = pass[i];
            for (Annotation pa : pas) {
                if (Alias.class.equals(pa.annotationType())) {
                    aliases[i] = ((Alias) pa).value();
                }
            }
        }
    }

    private String getParameterNameByIndex(int index) {
        String alias = aliases[index];
        return alias != null ? alias : String.valueOf(index + 1);
    }


}
