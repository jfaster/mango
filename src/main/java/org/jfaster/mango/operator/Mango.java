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

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.cache.CacheHandler;
import org.jfaster.mango.datasource.factory.DataSourceFactory;
import org.jfaster.mango.datasource.factory.SimpleDataSourceFactory;
import org.jfaster.mango.exception.IncorrectAnnotationException;
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
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * mango框架DAO工厂
 *
 * @author ash
 */
public class Mango {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(Mango.class);

    private final DataSourceFactory dataSourceFactory;
    private final JdbcOperations jdbcOperations;
    private final CacheHandler defaultCacheHandler;

    private ParameterNameDiscover parameterNameDiscover = new SerialNumberParameterNameDiscover();

    private final InterceptorChain queryInterceptorChain = new InterceptorChain();
    private final InterceptorChain updateInterceptorChain = new InterceptorChain();
    private final ConcurrentHashMap<Method, StatsCounter> statsCounterMap =
            new ConcurrentHashMap<Method, StatsCounter>();

    public Mango(DataSource dataSource) {
        this(new SimpleDataSourceFactory(dataSource));
    }

    public Mango(DataSourceFactory dataSourceFactory) {
        this(dataSourceFactory, (CacheHandler) null);
    }

    public Mango(DataSource dataSource, CacheHandler defaultCacheHandler) {
        this(new SimpleDataSourceFactory(dataSource), defaultCacheHandler);
    }

    public Mango(DataSourceFactory dataSourceFactory, CacheHandler defaultCacheHandler) {
        this.dataSourceFactory = dataSourceFactory;
        this.jdbcOperations = new JdbcTemplate();
        this.defaultCacheHandler = defaultCacheHandler;
    }

    public Mango(DataSource dataSource, JdbcOperations jdbcOperations) {
        this(new SimpleDataSourceFactory(dataSource), jdbcOperations);
    }

    public Mango(DataSourceFactory dataSourceFactory, JdbcOperations jdbcOperations) {
        this(dataSourceFactory, jdbcOperations, null);
    }

    public Mango(DataSource dataSource, JdbcOperations jdbcOperations,  CacheHandler defaultCacheHandler) {
        this(new SimpleDataSourceFactory(dataSource), jdbcOperations, defaultCacheHandler);
    }

    public Mango(DataSourceFactory dataSourceFactory, JdbcOperations jdbcOperations, CacheHandler defaultCacheHandler) {
        this.dataSourceFactory = dataSourceFactory;
        this.jdbcOperations = jdbcOperations;
        this.defaultCacheHandler = defaultCacheHandler;
    }


    /**
     * 创建代理DAO类
     */
    public <T> T create(Class<T> daoClass) {
        return create(daoClass, null);
    }

    /**
     * 创建代理DAO类，并对该类使用特定的{@link CacheHandler}
     */
    public <T> T create(Class<T> daoClass, @Nullable CacheHandler cacheHandler) {
        if (daoClass == null) {
            throw new NullPointerException("dao interface can't be null");
        }
        DB dbAnno = daoClass.getAnnotation(DB.class);
        if (dbAnno == null) {
            throw new IncorrectAnnotationException("dao interface expected one @DB " +
                    "annotation but not found");
        }
        if (cacheHandler == null) {
            cacheHandler = defaultCacheHandler;
        }
        return Reflection.newProxy(daoClass, new MangoInvocationHandler(this, cacheHandler));
    }


    public void addQueryInterceptor(Interceptor interceptor) {
        queryInterceptorChain.addInterceptor(interceptor);
    }

    public void addUpdateInterceptor(Interceptor interceptor) {
        updateInterceptorChain.addInterceptor(interceptor);
    }

    /**
     * 返回各个方法对应的状态
     */
    public Map<Method, MethodStats> getStatsMap() {
        Set<Map.Entry<Method, StatsCounter>> entrySet = statsCounterMap.entrySet();
        Map<Method, MethodStats> map = new HashMap<Method, MethodStats>();
        for (Map.Entry<Method, StatsCounter> entry : entrySet) {
            map.put(entry.getKey(), entry.getValue().snapshot());
        }
        return map;
    }

    private static class MangoInvocationHandler extends AbstractInvocationHandler implements InvocationHandler {

        private final JdbcOperations jdbcOperations;
        private final ConcurrentHashMap<Method,StatsCounter> statsCounterMap;
        private final OperatorFactory operatorFactory;
        private final ParameterNameDiscover parameterNameDiscover;

        private MangoInvocationHandler(Mango mango, @Nullable CacheHandler cacheHandler) {
            jdbcOperations = mango.jdbcOperations;
            statsCounterMap = mango.statsCounterMap;
            operatorFactory = new OperatorFactory(mango.dataSourceFactory, cacheHandler,
                    mango.queryInterceptorChain, mango.updateInterceptorChain);
            parameterNameDiscover = mango.parameterNameDiscover;
        }

        private final LoadingCache<Method, Operator> cache = new DoubleCheckCache<Method, Operator>(
                new CacheLoader<Method, Operator>() {
                    public Operator load(Method method) throws Exception {
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

    public ParameterNameDiscover getParameterNameDiscover() {
        return parameterNameDiscover;
    }

    public void setParameterNameDiscover(ParameterNameDiscover parameterNameDiscover) {
        this.parameterNameDiscover = parameterNameDiscover;
    }

}
