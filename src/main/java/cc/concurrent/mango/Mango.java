package cc.concurrent.mango;

import cc.concurrent.mango.operator.Operator;
import cc.concurrent.mango.operator.OperatorFactory;
import cc.concurrent.mango.util.ToStringHelper;
import cc.concurrent.mango.util.concurrent.CacheLoader;
import cc.concurrent.mango.util.concurrent.DoubleCheckCache;
import cc.concurrent.mango.util.concurrent.LoadingCache;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
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
    private final CacheHandler defaultCacheHandler;

    public Mango(DataSourceFactory dataSourceFactory, CacheHandler defaultCacheHandler) {
        this.dataSourceFactory = dataSourceFactory;
        this.defaultCacheHandler = defaultCacheHandler;
    }

    public <T> T create(Class<T> daoClass) {
        return create(daoClass, null);
    }

    public <T> T create(Class<T> daoClass, CacheHandler cacheHandler) {
        if (cacheHandler == null) {
            cacheHandler = defaultCacheHandler;
        }
        return Reflection.newProxy(daoClass, new MangoInvocationHandler(dataSourceFactory, cacheHandler));
    }

    private static class MangoInvocationHandler extends AbstractInvocationHandler implements InvocationHandler {

        private final DataSourceFactory dataSourceFactory;
        private final CacheHandler cacheHandler;

        private MangoInvocationHandler(DataSourceFactory dataSourceFactory, CacheHandler cacheHandler) {
            this.dataSourceFactory = dataSourceFactory;
            this.cacheHandler = cacheHandler;
        }

        private final LoadingCache<Method, Operator> cache = new DoubleCheckCache<Method, Operator>(
                new CacheLoader<Method, Operator>() {
                    public Operator load(Method method) throws Exception {
                        Operator operator = OperatorFactory.getOperator(method);
                        operator.setDataSourceFactory(dataSourceFactory);
                        operator.setCacheHandler(cacheHandler);
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
