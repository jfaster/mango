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

package org.jfaster.mango;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.datasource.DataSourceFactory;
import org.jfaster.mango.datasource.SimpleDataSourceFactory;
import org.jfaster.mango.exception.IncorrectAnnotationException;
import org.jfaster.mango.runtime.DataSourceFactoryHolder;
import org.jfaster.mango.runtime.operator.*;
import org.jfaster.mango.util.ToStringHelper;
import org.jfaster.mango.util.concurrent.CacheLoader;
import org.jfaster.mango.util.concurrent.DoubleCheckCache;
import org.jfaster.mango.util.concurrent.LoadingCache;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;
import org.jfaster.mango.util.reflect.AbstractInvocationHandler;
import org.jfaster.mango.util.reflect.Reflection;

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

    private final DataSourceFactoryHolder dataSourceFactoryHolder;
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
        this.dataSourceFactoryHolder = new DataSourceFactoryHolder(dataSourceFactory);
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
            throw new IncorrectAnnotationException("dao interface expected one @DB " +
                    "annotation but not found");
        }
        if (cacheHandler == null) {
            cacheHandler = defaultCacheHandler;
        }
        return Reflection.newProxy(daoClass,
                new MangoInvocationHandler(dataSourceFactoryHolder, cacheHandler, statsCounterMap));
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

    /**
     * 设置新的{@link DataSourceFactory}，实时生效
     */
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        dataSourceFactoryHolder.set(dataSourceFactory);
    }

    /**
     * 获得正在使用的{@link DataSourceFactory}
     */
    public DataSourceFactory getDataSourceFactory() {
        return dataSourceFactoryHolder.get();
    }

    private static class MangoInvocationHandler extends AbstractInvocationHandler implements InvocationHandler {

        private final DataSourceFactoryHolder dataSourceFactoryHolder;
        private final CacheHandler cacheHandler;
        private final ConcurrentHashMap<Method, StatsCounter> statsCounterMap;

        private MangoInvocationHandler(DataSourceFactoryHolder dataSourceFactoryHolder,
                                       @Nullable CacheHandler cacheHandler,
                                       ConcurrentHashMap<Method, StatsCounter> statsCounterMap) {
            this.dataSourceFactoryHolder = dataSourceFactoryHolder;
            this.cacheHandler = cacheHandler;
            this.statsCounterMap = statsCounterMap;
        }

        private final LoadingCache<Method, Operator> cache = new DoubleCheckCache<Method, Operator>(
                new CacheLoader<Method, Operator>() {
                    public CacheableOperator load(Method method) throws Exception {
                        StatsCounter statsCounter = getStatusCounter(method);
                        long now = System.nanoTime();
                        CacheableOperator operator = OperatorFactory.getOperator(method);
                        statsCounter.recordInit(System.nanoTime() - now);
                        operator.setDataSourceFactoryHolder(dataSourceFactoryHolder);
                        operator.setCacheHandler(cacheHandler);
                        operator.setStatsCounter(statsCounter);
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
