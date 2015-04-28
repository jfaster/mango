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

package org.jfaster.mango.operator.cache;

import org.jfaster.mango.exception.IncorrectSqlException;
import org.jfaster.mango.exception.NotReadablePropertyException;
import org.jfaster.mango.invoker.GetterInvoker;
import org.jfaster.mango.invoker.InvokerCache;
import org.jfaster.mango.operator.InvocationContext;
import org.jfaster.mango.operator.QueryOperator;
import org.jfaster.mango.parser.ASTJDBCIterableParameter;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.reflect.MethodDescriptor;
import org.jfaster.mango.util.Iterables;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import java.util.*;

/**
 * @author ash
 */
public class CacheableQueryOperator extends QueryOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(CacheableUpdateOperator.class);

    private CacheDriver driver;

    GetterInvoker propertyOfMapperInvoker;

    public CacheableQueryOperator(ASTRootNode rootNode, MethodDescriptor md, CacheDriver cacheDriver) {
        super(rootNode, md);

        this.driver = cacheDriver;

        List<ASTJDBCIterableParameter> jips = rootNode.getJDBCIterableParameters();
        if (jips.size() > 1) {
            throw new IncorrectSqlException("if use cache, sql's in clause expected less than or equal 1 but " +
                    jips.size()); // sql中不能有多个in语句
        }

        if (driver.isUseMultipleKeys()) {
            String propertyOfMapper = driver.getPropertyOfMapper().toLowerCase(); //可能有下划线
            List<GetterInvoker> invokers = InvokerCache.getGetterInvokers(mappedClass);
            for (GetterInvoker invoker : invokers) {
                if (Strings.underscoreName(invoker.getName()).equals(propertyOfMapper)) {
                    propertyOfMapperInvoker = invoker;
                }
            }
            if (propertyOfMapperInvoker == null) {
                // 如果使用cache并且sql中有一个in语句，mappedClass必须含有特定属性，必须a in (...)，则mappedClass必须含有a属性
                throw new NotReadablePropertyException("if use cache and sql has one in clause, property "
                        + propertyOfMapper + " of " + mappedClass + " expected readable but not");
            }
        }
    }

    @Override
    public Object execute(Object[] values) {
        InvocationContext context = invocationContextFactory.newInvocationContext(values);
        return driver.isUseMultipleKeys() ?
                multipleKeysCache(context, rowMapper.getMappedClass(), driver.getOnlyCacheByClass()) :
                singleKeyCache(context);
    }

    private <T, U> Object multipleKeysCache(InvocationContext context, Class<T> mappedClass, Class<U> cacheByActualClass) {
        boolean isDebugEnabled = logger.isDebugEnabled();
        Set<String> keys = driver.getCacheKeys(context);
        Map<String, Object> cachedResults = driver.getBulkFromCache(keys);
        AddableObject<T> addableObj = new AddableObject<T>(keys.size(), mappedClass);
        int hitCapacity = cachedResults != null ? cachedResults.size() : 0;
        List<U> hitCacheByActualObjs = new ArrayList<U>(hitCapacity); // 用于debug
        int hitNum = 0;
        Set<U> missCacheByActualObjs = new HashSet<U>(Math.max(1, keys.size() - hitCapacity) * 2);
        for (Object cacheByActualObj : new Iterables(driver.getOnlyCacheByObj(context))) {
            String key = driver.getCacheKey(cacheByActualObj);
            Object value = cachedResults != null ? cachedResults.get(key) : null;
            if (value == null) {
                missCacheByActualObjs.add(cacheByActualClass.cast(cacheByActualObj));
            } else {
                addableObj.add(mappedClass.cast(value));
                hitNum++;
                if (isDebugEnabled) {
                    hitCacheByActualObjs.add(cacheByActualClass.cast(cacheByActualObj));
                }
            }
        }
        statsCounter.recordHits(hitNum);
        statsCounter.recordMisses(missCacheByActualObjs.size());
        if (isDebugEnabled) {
            logger.debug("cache hit #keys={} #values={}", hitCacheByActualObjs, addableObj);
            logger.debug("cache miss #keys={}", missCacheByActualObjs);
        }
        if (!missCacheByActualObjs.isEmpty()) { // 有key没有命中
            driver.setOnlyCacheByObj(context, missCacheByActualObjs);
            Object dbValues = execute(context);
            for (Object dbValue : new Iterables(dbValues)) {
                // db数据添加入结果
                addableObj.add(mappedClass.cast(dbValue));
                // 添加入缓存
                Object propertyObj = propertyOfMapperInvoker.invoke(dbValue);
                if (propertyObj == null) {
                    throw new NullPointerException("property " + propertyOfMapperInvoker.getName() + " of " +
                            mappedClass + " is null, please check return type");
                }
                String key = driver.getCacheKey(propertyObj);
                driver.setToCache(key, dbValue);
            }
        }
        return addableObj.getReturn();
    }

    private Object singleKeyCache(InvocationContext context) {
        String key = driver.getCacheKey(context);
        Object value = driver.getFromCache(key);
        if (value == null) {
            statsCounter.recordMisses(1);
            if (logger.isDebugEnabled()) {
                logger.debug("cache miss #key＝{}", key);
            }
            value = execute(context);
            if (value != null) {
                driver.setToCache(key, value);
            }
        } else {
            statsCounter.recordHits(1);
            if (logger.isDebugEnabled()) {
                logger.debug("cache hit #key={} #value={}", key, value);
            }
        }
        return value;
    }

    private class AddableObject<T> {

        List<T> hitValueList;
        Set<T> hitValueSet;
        Class<T> valueClass;

        private AddableObject(int initialCapacity, Class<T> valueClass) {
            if (isForSet) {
                hitValueSet = new HashSet<T>(initialCapacity * 2);
            } else { // 返回List或数组或单个值都先使用List
                hitValueList = new LinkedList<T>();
            }
            this.valueClass = valueClass;
        }

        public void add(T v) {
            if (hitValueList != null) {
                hitValueList.add(v);
            } else {
                hitValueSet.add(v);
            }
        }

        public Object getReturn() {
            if (isForList) {
                return hitValueList;
            } else if (isForSet) {
                return hitValueSet;
            } else if (isForArray) {
                return org.jfaster.mango.util.Arrays.toArray(hitValueList, valueClass);
            } else {
                return !hitValueList.isEmpty() ? hitValueList.get(0) : null;
            }
        }

        @Override
        public String toString() {
            return hitValueList != null ? hitValueList.toString() : hitValueSet.toString();
        }
    }

}
