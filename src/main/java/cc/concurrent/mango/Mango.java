package cc.concurrent.mango;

import cc.concurrent.mango.operator.Operator;
import cc.concurrent.mango.operator.OperatorFactory;
import cc.concurrent.mango.util.ToStringHelper;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.AbstractInvocationHandler;
import com.google.common.reflect.Reflection;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author ash
 */
public class Mango {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(Mango.class);

    private final DataSourceFactory dataSourceFactory;
    private final DataCache defaultDataCache;

    public Mango(DataSourceFactory dataSourceFactory, DataCache defaultDataCache) {
        this.dataSourceFactory = dataSourceFactory;
        this.defaultDataCache = defaultDataCache;
    }

    public <T> T create(Class<T> daoClass) {
        return create(daoClass, null);
    }

    public <T> T create(Class<T> daoClass, DataCache dataCache) {
        if (dataCache == null) {
            dataCache = defaultDataCache;
        }
        return Reflection.newProxy(daoClass, new MangoInvocationHandler(dataSourceFactory, dataCache));
    }

    private static class MangoInvocationHandler extends AbstractInvocationHandler implements InvocationHandler {

        private final DataSourceFactory dataSourceFactory;
        private final DataCache dataCache;

        private MangoInvocationHandler(DataSourceFactory dataSourceFactory, DataCache dataCache) {
            this.dataSourceFactory = dataSourceFactory;
            this.dataCache = dataCache;
        }

        private final LoadingCache<Method, Operator> cache = CacheBuilder.newBuilder()
                .build(
                        new CacheLoader<Method, Operator>() {
                            public Operator load(Method method) throws Exception {
                                Operator operator = OperatorFactory.getOperator(method);
                                operator.setDataSourceFactory(dataSourceFactory);
                                operator.setDataCache(dataCache);
                                return operator;
                            }
                        });

        @Override
        protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
            if (logger.isDebugEnabled()) {
                logger.debug("{} #args={}", ToStringHelper.toString(method), args);
            }
            Operator operator = cache.get(method);
            Object r = operator.execute(args);
            if (logger.isDebugEnabled()) {
                logger.debug("{} #result={}", ToStringHelper.toString(method), r);
            }
            return r;
        }

    }

}
