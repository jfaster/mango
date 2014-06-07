/*
 * Copyright 2014 mango.concurrent.cc
 *
 * The Netty Project licenses this file to you under the Apache License,
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

package cc.concurrent.mango;

import cc.concurrent.mango.exception.IncorrectAnnotationException;
import cc.concurrent.mango.runtime.operator.*;
import cc.concurrent.mango.util.ToStringHelper;
import cc.concurrent.mango.util.concurrent.CacheLoader;
import cc.concurrent.mango.util.concurrent.DoubleCheckCache;
import cc.concurrent.mango.util.concurrent.LoadingCache;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import cc.concurrent.mango.util.reflect.AbstractInvocationHandler;
import cc.concurrent.mango.util.reflect.Reflection;

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
    private final CacheHandler defaultCacheHandler;
    private final ConcurrentHashMap<Method, StatsCounter> statsCounterMap;

    public Mango(DataSource dataSource) {
        this(new SimpleDataSourceFactory(dataSource));
    }

    public Mango(DataSourceFactory dataSourceFactory) {
        this(dataSourceFactory, null);
    }

    public Mango(DataSource dataSource, CacheHandler defaultCacheHandler) {
        this(new SimpleDataSourceFactory(dataSource), defaultCacheHandler);
    }

    public Mango(DataSourceFactory dataSourceFactory, CacheHandler defaultCacheHandler) {
        this.dataSourceFactory = dataSourceFactory;
        this.defaultCacheHandler = defaultCacheHandler;
        this.statsCounterMap = new ConcurrentHashMap<Method, StatsCounter>();
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
            throw new IncorrectAnnotationException("dao interface expected one cc.concurrent.mango.DB " +
                    "annotation but not found");
        }
        if (cacheHandler == null) {
            cacheHandler = defaultCacheHandler;
        }
        return Reflection.newProxy(daoClass,
                new MangoInvocationHandler(dataSourceFactory, cacheHandler, statsCounterMap));
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

        private final DataSourceFactory dataSourceFactory;
        private final CacheHandler cacheHandler;
        private final ConcurrentHashMap<Method, StatsCounter> statsCounterMap;

        private MangoInvocationHandler(DataSourceFactory dataSourceFactory,
                                       @Nullable CacheHandler cacheHandler,
                                       ConcurrentHashMap<Method, StatsCounter> statsCounterMap) {
            this.dataSourceFactory = dataSourceFactory;
            this.cacheHandler = cacheHandler;
            this.statsCounterMap = statsCounterMap;
        }

        private final LoadingCache<Method, Operator> cache = new DoubleCheckCache<Method, Operator>(
                new CacheLoader<Method, Operator>() {
                    public CacheableOperator load(Method method) throws Exception {
                        CacheableOperator operator = OperatorFactory.getOperator(method);
                        operator.setDataSourceFactory(dataSourceFactory);
                        operator.setCacheHandler(cacheHandler);
                        operator.setStatsCounter(getStatusCounter(method));
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
                statsCounter = new SimpleStatsCounter();
                StatsCounter old = statsCounterMap.putIfAbsent(method, statsCounter);
                if (old != null) { // 已经存在，就用老的，这样能保证单例
                    statsCounter = old;
                }
            }
            return statsCounter;
        }

    }

}
