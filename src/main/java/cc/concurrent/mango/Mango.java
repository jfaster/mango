package cc.concurrent.mango;

import cc.concurrent.mango.operator.Operator;
import cc.concurrent.mango.operator.OperatorFactory;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.reflect.Reflection;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/**
 * @author ash
 */
public class Mango {

    private final DataSource dataSource;

    public Mango(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public <T> T create(Class<T> daoClass) {
        return Reflection.newProxy(daoClass, new MangoInvocationHandler(this));
    }

    protected Object handleInvocation(Method method, Object[] args) throws Throwable {
        Operator operator = cache.get(method);
        return operator.execute(dataSource, args);
    }

    LoadingCache<Method, Operator> cache = CacheBuilder.newBuilder()
            .build(
                    new CacheLoader<Method, Operator>() {
                        public Operator load(Method method) throws Exception{
                            return OperatorFactory.getOperator(method);
                        }
                    });

}
