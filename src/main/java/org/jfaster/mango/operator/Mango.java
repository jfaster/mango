/*
 * Copyright 2014 mango.jfaster.org
 *
 * The Mango Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.jfaster.mango.operator;

import org.jfaster.mango.annotation.Cache;
import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.datasource.DataSourceType;
import org.jfaster.mango.operator.cache.CacheHandler;
import org.jfaster.mango.datasource.DataSourceFactory;
import org.jfaster.mango.datasource.SimpleDataSourceFactory;
import org.jfaster.mango.exception.InitializationException;
import org.jfaster.mango.jdbc.JdbcOperations;
import org.jfaster.mango.jdbc.JdbcTemplate;
import org.jfaster.mango.reflect.*;
import org.jfaster.mango.util.ToStringHelper;
import org.jfaster.mango.util.concurrent.cache.CacheLoader;
import org.jfaster.mango.util.concurrent.cache.DoubleCheckCache;
import org.jfaster.mango.util.concurrent.cache.LoadingCache;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * mango框架DAO工厂
 *
 * @author ash
 */
public class Mango {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(Mango.class);

    /**
     * 数据源工厂
     */
    private DataSourceFactory dataSourceFactory;

    /**
     * 全局缓存处理器
     */
    private CacheHandler defaultCacheHandler;

    /**
     * 全局懒加载，默认为false
     */
    private boolean defaultLazyInit = false;

    /**
     * 拦截器链，默认为空
     */
    private InterceptorChain interceptorChain = new InterceptorChain();

    /**
     * jdbc操作
     */
    private JdbcOperations jdbcOperations = new JdbcTemplate();

    /**
     * 参数名发现器
     */
    private ParameterNameDiscover parameterNameDiscover = new SerialNumberParameterNameDiscover();

    /**
     * 统计map
     */
    private final ConcurrentHashMap<Method, StatsCounter> statsCounterMap =
            new ConcurrentHashMap<Method, StatsCounter>();

    /**
     * mango实例
     */
    private final static CopyOnWriteArrayList<Mango> instances = new CopyOnWriteArrayList<Mango>();

    public synchronized static Mango newInstance() {
        if (instances.size() == 1) {
            if (logger.isWarnEnabled()) {
                logger.warn("Find out more mango instances, it is recommended to use only one");
            }
        }
        Mango mango = new Mango();
        instances.add(mango);
        return mango;
    }

    public static Mango newInstance(DataSource dataSource) {
        return newInstance().setDataSource(dataSource);
    }

    public static Mango newInstance(DataSourceFactory dataSourceFactory) {
        return newInstance().setDataSourceFactory(dataSourceFactory);
    }

    /**
     * 获得mango实例
     */
    public static List<Mango> getInstances() {
        List<Mango> mangos = new ArrayList<Mango>();
        for (Mango instance : instances) {
            mangos.add(instance);
        }
        return Collections.unmodifiableList(mangos);
    }

    /**
     * 添加拦截器
     */
    public Mango addInterceptor(Interceptor interceptor) {
        if (interceptor == null) {
            throw new NullPointerException("interceptor can't be null");
        }
        if (interceptorChain == null) {
            interceptorChain = new InterceptorChain();
        }
        interceptorChain.addInterceptor(interceptor);
        return this;
    }

    /**
     * 创建代理DAO类
     */
    public <T> T create(Class<T> daoClass) {
        return create(daoClass, defaultCacheHandler, defaultLazyInit);
    }

    /**
     * 创建代理DAO类，使用特定的{@link CacheHandler}
     */
    public <T> T create(Class<T> daoClass, @Nullable CacheHandler cacheHandler) {
        return create(daoClass, cacheHandler, defaultLazyInit);
    }

    /**
     * 创建代理DAO类，自定义是否懒加载
     */
    public <T> T create(Class<T> daoClass, boolean lazyInit) {
        return create(daoClass, defaultCacheHandler, lazyInit);
    }

    /**
     * 创建代理DAO类，使用特定的{@link CacheHandler}，自定义是否懒加载
     */
    public <T> T create(Class<T> daoClass, @Nullable CacheHandler cacheHandler, boolean lazyInit) {
        if (daoClass == null) {
            throw new NullPointerException("dao interface can't be null");
        }

        if (!daoClass.isInterface()) {
            throw new IllegalArgumentException("expected an interface to proxy, but " + daoClass);
        }

        DB dbAnno = daoClass.getAnnotation(DB.class);
        if (dbAnno == null) {
            throw new IllegalStateException("dao interface expected one @DB " +
                    "annotation but not found");
        }

        if (cacheHandler == null) {
            cacheHandler = defaultCacheHandler;
        }
        Cache cacheAnno = daoClass.getAnnotation(Cache.class);
        if (cacheAnno != null && cacheHandler == null) {
            throw new IllegalStateException("if @Cache annotation on dao interface, " +
                    "cacheHandler can't be null");
        }

        if (dataSourceFactory == null) {
            throw new IllegalArgumentException("dataSourceFactory can't be null");
        }

        MangoInvocationHandler handler = new MangoInvocationHandler(this, cacheHandler);
        if (!lazyInit) { // 不使用懒加载，则提前加载
            Method[] methods = daoClass.getMethods();
            for (Method method : methods) {
                try {
                    handler.getOperator(method);
                } catch (Throwable e) {
                    throw new InitializationException("initialize " + ToStringHelper.toString(method) + " error", e);
                }
            }
        }
        return Reflection.newProxy(daoClass, handler);
    }

    /**
     * 返回各个方法对应的状态
     */
    public List<OperatorStats> getAllStats() {
        List<OperatorStats> oss = new ArrayList<OperatorStats>();
        Set<Map.Entry<Method, StatsCounter>> entrySet = statsCounterMap.entrySet();
        for (Map.Entry<Method, StatsCounter> entry : entrySet) {
            Method method = entry.getKey();
            OperatorStats os = entry.getValue().snapshot();
            os.setMethod(method);
            oss.add(os);
        }

        // 按照总执行次数从高到低排序
        Collections.sort(oss, new Comparator<OperatorStats>() {
            @Override
            public int compare(OperatorStats o1, OperatorStats o2) {
                long c1 = o1.getExecuteCount();
                long c2 = o2.getExecuteCount();
                return (c1 < c2) ? 1 : ((c1 > c2) ? -1 : 0);
            }
        });
        return oss;
    }

    /**
     * 重置各个方法的状态
     */
    public void resetAllStats() {
        Set<Map.Entry<Method, StatsCounter>> entrySet = statsCounterMap.entrySet();
        for (Map.Entry<Method, StatsCounter> entry : entrySet) {
            entry.getValue().reset();
        }
    }

    /**
     * 根据数据源名字获得主库数据源
     */
    public DataSource getMasterDataSource(String dataSourceName) {
        return dataSourceFactory.getDataSource(dataSourceName, DataSourceType.MASTER);
    }

    private static class MangoInvocationHandler extends AbstractInvocationHandler implements InvocationHandler {

        private final JdbcOperations jdbcOperations;
        private final ConcurrentHashMap<Method, StatsCounter> statsCounterMap;
        private final OperatorFactory operatorFactory;
        private final ParameterNameDiscover parameterNameDiscover;

        private final LoadingCache<Method, Operator> cache = new DoubleCheckCache<Method, Operator>(
                new CacheLoader<Method, Operator>() {
                    public Operator load(Method method) {
                        if (logger.isInfoEnabled()) {
                            logger.info("initializing operator for {}", ToStringHelper.toString(method));
                        }
                        StatsCounter statsCounter = getStatusCounter(method);
                        long now = System.nanoTime();
                        MethodDescriptor md = Methods.getMethodDescriptor(method, parameterNameDiscover);
                        Operator operator = operatorFactory.getOperator(md);
                        operator.setJdbcOperations(jdbcOperations);
                        operator.setStatsCounter(statsCounter);
                        statsCounter.recordInit(System.nanoTime() - now);
                        return operator;
                    }
                });

        private MangoInvocationHandler(Mango mango, @Nullable CacheHandler cacheHandler) {
            jdbcOperations = mango.jdbcOperations;
            statsCounterMap = mango.statsCounterMap;
            operatorFactory = new OperatorFactory(mango.dataSourceFactory, cacheHandler, mango.interceptorChain);
            parameterNameDiscover = mango.parameterNameDiscover;
        }

        @Override
        protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
            if (logger.isDebugEnabled()) {
                logger.debug("{} #args={}", ToStringHelper.toString(method), args);
            }
            Operator operator = getOperator(method);
            Object r = operator.execute(args);
            if (logger.isDebugEnabled()) {
                logger.debug("{} #result={}", ToStringHelper.toString(method), r);
            }
            return r;
        }

        Operator getOperator(Method method) {
            return cache.get(method);
        }

        private StatsCounter getStatusCounter(Method method) {
            StatsCounter statsCounter = statsCounterMap.get(method);
            if (statsCounter == null) {
                statsCounter = new StatsCounter();
                StatsCounter old = statsCounterMap.putIfAbsent(method, statsCounter);
                if (old != null) { // 已经存在，就用老的，这样能保证单例
                    statsCounter = old;
                }
            }
            return statsCounter;
        }

    }

    public Mango setDataSource(DataSource dataSource) {
        if (dataSource == null) {
            throw new NullPointerException("dataSource can't be null");
        }
        dataSourceFactory = new SimpleDataSourceFactory(dataSource);
        return this;
    }

    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactory;
    }

    public Mango setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        if (dataSourceFactory == null) {
            throw new NullPointerException("dataSourceFactory can't be null");
        }
        this.dataSourceFactory = dataSourceFactory;
        return this;
    }

    public CacheHandler getDefaultCacheHandler() {
        return defaultCacheHandler;
    }

    public Mango setDefaultCacheHandler(CacheHandler defaultCacheHandler) {
        if (defaultCacheHandler == null) {
            throw new NullPointerException("defaultCacheHandler can't be null");
        }
        this.defaultCacheHandler = defaultCacheHandler;
        return this;
    }

    public boolean isDefaultLazyInit() {
        return defaultLazyInit;
    }

    public Mango setDefaultLazyInit(boolean defaultLazyInit) {
        this.defaultLazyInit = defaultLazyInit;
        return this;
    }

    public Mango setInterceptorChain(InterceptorChain interceptorChain) {
        if (interceptorChain == null) {
            throw new NullPointerException("interceptorChain can't be null");
        }
        this.interceptorChain = interceptorChain;
        return this;
    }

    public JdbcOperations getJdbcOperations() {
        return jdbcOperations;
    }

    public Mango setJdbcOperations(JdbcOperations jdbcOperations) {
        if (jdbcOperations == null) {
            throw new NullPointerException("jdbcOperations can't be null");
        }
        this.jdbcOperations = jdbcOperations;
        return this;
    }

    public ParameterNameDiscover getParameterNameDiscover() {
        return parameterNameDiscover;
    }

    public Mango setParameterNameDiscover(ParameterNameDiscover parameterNameDiscover) {
        if (parameterNameDiscover == null) {
            throw new NullPointerException("parameterNameDiscover can't be null");
        }
        this.parameterNameDiscover = parameterNameDiscover;
        return this;
    }

}
