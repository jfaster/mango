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
    private final DataCache dataCache;

    public Mango(DataSource dataSource, DataCache dataCache) {
        this.dataSource = dataSource;
        this.dataCache = dataCache;
    }

    public <T> T create(Class<T> daoClass) {
        String cacheKeyPrefix = null;
        Cache cacheAnno = daoClass.getAnnotation(Cache.class);
        if (cacheAnno != null) {
            cacheKeyPrefix = cacheAnno.prefix();
        }
        return Reflection.newProxy(daoClass, new MangoInvocationHandler(dataSource, dataCache, cacheKeyPrefix));
    }

    private class MangoInvocationHandler extends AbstractInvocationHandler implements InvocationHandler {

        private DataSource dataSource;
        private final DataCache dataCache;
        private final String cacheKeyPrefix;

        private MangoInvocationHandler(DataSource dataSource, DataCache dataCache, String cacheKeyPrefix) {
            this.dataSource = dataSource;
            this.dataCache = dataCache;
            this.cacheKeyPrefix = cacheKeyPrefix;
        }

        LoadingCache<Method, Operator> cache = CacheBuilder.newBuilder()
                .build(
                        new CacheLoader<Method, Operator>() {
                            public Operator load(Method method) throws Exception {
                                Operator operator = OperatorFactory.getOperator(method, cacheKeyPrefix);
                                operator.setDataSource(dataSource);
                                operator.setDataCache(dataCache);
                                return operator;
                            }
                        });

        @Override
        protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
            Operator operator = cache.get(method);
            return operator.execute(dataSource, args);
        }

    }

}
