package cc.concurrent.mango;

import cc.concurrent.mango.operator.Operator;
import cc.concurrent.mango.operator.OperatorFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.Reflection;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author ash
 */
public class Mango {

    private final DataSource dataSource;
    private final DataCache defaultDataCache;

    public Mango(DataSource dataSource, DataCache defaultDataCache) {
        this.dataSource = dataSource;
        this.defaultDataCache = defaultDataCache;
    }

    public <T> T create(Class<T> daoClass) {
        return create(daoClass, null);
    }

    public <T> T create(Class<T> daoClass, DataCache dataCache) {
        if (dataCache == null) {
            dataCache = defaultDataCache;
        }
        return Reflection.newProxy(daoClass, new MangoInvocationHandler(dataSource, dataCache, daoClass));
    }

    private static class MangoInvocationHandler extends AbstractInvocationHandler implements InvocationHandler {

        private DataSource dataSource;
        private final DataCache dataCache;
        private final Class<?> daoClass;

        private MangoInvocationHandler(DataSource dataSource, DataCache dataCache, Class<?> daoClass) {
            this.dataSource = dataSource;
            this.dataCache = dataCache;
            this.daoClass = daoClass;
        }

        LoadingCache<Method, Operator> cache = CacheBuilder.newBuilder()
                .build(
                        new CacheLoader<Method, Operator>() {
                            public Operator load(Method method) throws Exception {
                                CacheDescriptor cacheDescriptor = getCacheDescriptor(method);
                                Operator operator = OperatorFactory.getOperator(method);
                                operator.setDataSource(dataSource);
                                operator.setDataCache(dataCache);
                                operator.setCacheDescriptor(cacheDescriptor);
                                return operator;
                            }

                            private CacheDescriptor getCacheDescriptor(Method method) {
                                Cache cacheAnno = daoClass.getAnnotation(Cache.class);
                                CacheDescriptor cacheDescriptor = new CacheDescriptor();
                                if (cacheAnno != null) { // dao类使用cache
                                    CacheIgnored cacheIgnoredAnno = method.getAnnotation(CacheIgnored.class);
                                    if (cacheIgnoredAnno == null) { // method不禁用cache
                                        cacheDescriptor.setUseCache(true);
                                        cacheDescriptor.setPrefix(cacheAnno.prefix());
                                        Annotation[][] pass = method.getParameterAnnotations();
                                        for (int i = 0; i < pass.length; i++) {
                                            Annotation[] pas = pass[i];
                                            for (Annotation pa : pas) {
                                                if (CacheBy.class.equals(pa.annotationType())) {
                                                    cacheDescriptor.setBeanName(String.valueOf(i + 1));
                                                    cacheDescriptor.setPropertyName(((CacheBy) pa).value());
                                                }
                                            }
                                        }
                                    }
                                }
                                return cacheDescriptor;
                            }
                        });

        @Override
        protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
            Operator operator = cache.get(method);
            return operator.execute(args);
        }

    }

}
