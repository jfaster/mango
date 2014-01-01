package cc.concurrent.mango;

import cc.concurrent.mango.operator.Operator;
import cc.concurrent.mango.operator.OperatorFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.Reflection;

import javax.sql.DataSource;
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
        Cache cacheAnno = daoClass.getAnnotation(Cache.class);
        CacheDescriptor cacheDescriptor = new CacheDescriptor();
        if (cacheAnno != null) {
            cacheDescriptor.setUseCache(true);
            cacheDescriptor.setPrefix(cacheAnno.prefix());
        }
        if (dataCache == null) {
            dataCache = defaultDataCache;
        }
        return Reflection.newProxy(daoClass, new MangoInvocationHandler(dataSource, dataCache, cacheDescriptor));
    }

    private static class MangoInvocationHandler extends AbstractInvocationHandler implements InvocationHandler {

        private DataSource dataSource;
        private final DataCache dataCache;
        private final CacheDescriptor cacheDescriptor;

        private MangoInvocationHandler(DataSource dataSource, DataCache dataCache, CacheDescriptor cacheDescriptor) {
            this.dataSource = dataSource;
            this.dataCache = dataCache;
            this.cacheDescriptor = cacheDescriptor;
        }

        LoadingCache<Method, Operator> cache = CacheBuilder.newBuilder()
                .build(
                        new CacheLoader<Method, Operator>() {
                            public Operator load(Method method) throws Exception {

                                Operator operator = OperatorFactory.getOperator(method);
                                operator.setDataSource(dataSource);
                                operator.setDataCache(dataCache);
                                return operator;
                            }

                            
                        });

        @Override
        protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
            Operator operator = cache.get(method);
            return operator.execute(args);
        }

    }

}
