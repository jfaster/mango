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

import org.jfaster.mango.exception.IncorrectReturnTypeException;
import org.jfaster.mango.exception.IncorrectSqlException;
import org.jfaster.mango.exception.NotReadablePropertyException;
import org.jfaster.mango.exception.UnreachableCodeException;
import org.jfaster.mango.interceptor.InterceptorChain;
import org.jfaster.mango.jdbc.BeanPropertyRowMapper;
import org.jfaster.mango.jdbc.JdbcUtils;
import org.jfaster.mango.jdbc.RowMapper;
import org.jfaster.mango.jdbc.SingleColumnRowMapper;
import org.jfaster.mango.parser.node.ASTJDBCIterableParameter;
import org.jfaster.mango.parser.node.ASTRootNode;
import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.support.SQLType;
import org.jfaster.mango.util.Iterables;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;
import org.jfaster.mango.util.reflect.BeanInfoCache;
import org.jfaster.mango.util.reflect.Beans;
import org.jfaster.mango.util.reflect.TypeToken;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 处理所有的查询操作
 *
 * @author ash
 */
public class QueryOperator extends CacheableOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(QueryOperator.class);

    private RowMapper<?> rowMapper;
    private boolean isForList;
    private boolean isForSet;
    private boolean isForArray;
    private String interableProperty; // "a in (:1)"中的a

    public QueryOperator(ASTRootNode rootNode, Method method, SQLType sqlType, InterceptorChain interceptorChain) {
        super(rootNode, method, sqlType, interceptorChain);
        init();
    }

    private void init() {
        TypeToken typeToken = new TypeToken(method.getGenericReturnType());
        isForList = typeToken.isList();
        isForSet = typeToken.isSet();
        isForArray = typeToken.isArray();
        Class<?> mappedClass = typeToken.getMappedClass();
        rowMapper = getRowMapper(mappedClass);

        List<ASTJDBCIterableParameter> jips = rootNode.getJDBCIterableParameters();
        if (!jips.isEmpty() && !isForList && !isForSet && !isForArray) {
            throw new IncorrectReturnTypeException("if sql has in clause, return type " +
                    "expected array or implementations of java.util.List or implementations of java.util.Set " +
                    "but " + method.getGenericReturnType()); // sql中使用了in查询，返回参数必须可迭代
        }
        if (isUseCache() && isUseMultipleKeys()) {
            interableProperty = getInterableProperty();
            Method readMethod = BeanInfoCache.getReadMethod(mappedClass, interableProperty);
            if (readMethod == null) {
                // 如果使用cache并且sql中有一个in语句，mappedClass必须含有特定属性，必须a in (...)，则mappedClass必须含有a属性
                throw new NotReadablePropertyException("if use cache and sql has one in clause, property "
                        + interableProperty + " of " + mappedClass + " expected readable but not");
            }
        }
    }

    @Override
    Type[] getMethodArgTypes(Method method) {
        return method.getGenericParameterTypes();
    }

    @Override
    protected void cacheInitPostProcessor() {
        if (isUseCache()) {
            List<ASTJDBCIterableParameter> jips = rootNode.getJDBCIterableParameters();
            if (jips.size() > 1) {
                throw new IncorrectSqlException("if use cache, sql's in clause expected less than or equal 1 but " +
                        jips.size()); // sql中不能有多个in语句
            }
        }
    }

    @Override
    public Object execute(Object[] methodArgs) {
        RuntimeContext context = buildRuntimeContext(methodArgs);
        if (isUseCache()) { // 先使用缓存，再使用db
            return isUseMultipleKeys() ?
                    multipleKeysCache(context, rowMapper.getMappedClass(), getSuffixClass()) :
                    singleKeyCache(context);
        } else { // 直接使用db
            return executeFromDb(context);
        }
    }

    private <T, U> Object multipleKeysCache(RuntimeContext context, Class<T> mappedClass, Class<U> suffixClass) {
        boolean isDebugEnabled = logger.isDebugEnabled();
        Set<String> keys = getCacheKeys(context);
        Map<String, Object> cacheResults = getBulkFromCache(keys);
        AddableObject<T> addableObj = new AddableObject<T>(keys.size(), mappedClass);
        int hitCapacity = cacheResults != null ? cacheResults.size() : 0;
        List<U> hitSuffix = new ArrayList<U>(hitCapacity); // 用于debug
        Set<U> missSuffix = new HashSet<U>((keys.size() - hitCapacity) * 2);
        for (Object suffix : new Iterables(getSuffixObj(context))) {
            String key = getCacheKey(suffix);
            Object value = cacheResults != null ? cacheResults.get(key) : null;
            if (value == null) {
                missSuffix.add(suffixClass.cast(suffix));
            } else {
                addableObj.add(mappedClass.cast(value));
                if (isDebugEnabled) {
                    hitSuffix.add(suffixClass.cast(suffix));
                }
            }
        }
        if (isDebugEnabled) {
            logger.debug("cache hit #keys={} #values={}", hitSuffix, addableObj);
            logger.debug("cache miss #keys={}", missSuffix);
        }
        if (!missSuffix.isEmpty()) { // 有key没有命中
            setSuffixObj(context, missSuffix);
            Object dbValues = executeFromDb(context);
            for (Object dbValue : new Iterables(dbValues)) {
                // db数据添加入结果
                addableObj.add(mappedClass.cast(dbValue));
                // 添加入缓存
                Object suffix = Beans.getPropertyValue(dbValue, interableProperty, mappedClass);
                String key = getCacheKey(suffix);
                setToCache(key, dbValue);
            }
        }
        return addableObj.getReturn();
    }

    private Object singleKeyCache(RuntimeContext context) {
        String key = getCacheKey(context);
        Object value = getFromCache(key);
        if (value == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("cache miss #key＝{}", key);
            }
            value = executeFromDb(context);
            if (value != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("cache set #key={} #value={}", key, value);
                }
                setToCache(key, value);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("cache hit #key={} #value={}", key, value);
            }
        }
        return value;
    }

    private Object executeFromDb(RuntimeContext context) {
        DataSource ds = getDataSource(context);
        rootNode.render(context);
        String sql = context.getSql();
        Object[] args = context.getArgs();
        handleByInterceptorChain(sql, args);
        Object r;
        boolean success = false;
        long now = System.nanoTime();
        try {
            if (isForList) {
                r = jdbcTemplate.queryForList(ds, sql, args, rowMapper);
            } else if (isForSet) {
                r = jdbcTemplate.queryForSet(ds, sql, args, rowMapper);
            } else if (isForArray) {
                r= jdbcTemplate.queryForArray(ds, sql, args, rowMapper);
            } else {
                r = jdbcTemplate.queryForObject(ds, sql, args, rowMapper);
            }
            success = true;
        } finally {
            long cost = System.nanoTime() - now;
            if (success) {
                statsCounter.recordExecuteSuccess(cost);
            } else {
                statsCounter.recordExecuteException(cost);
            }
        }
        return r;
    }

    private static <T> RowMapper<T> getRowMapper(Class<T> clazz) {
        return JdbcUtils.isSingleColumnClass(clazz) ?
                new SingleColumnRowMapper<T>(clazz) :
                new BeanPropertyRowMapper<T>(clazz);
    }

    private class AddableObject<T> {

        List<T> hitValueList = null;
        Set<T> hitValueSet = null;
        Class<T> valueClass;

        private AddableObject(int initialCapacity, Class<T> valueClass) {
            if (isForSet) {
                hitValueSet = new HashSet<T>(initialCapacity * 2);
            } else { // 返回List或数组都先使用List
                hitValueList = new ArrayList<T>(initialCapacity);
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
                throw new UnreachableCodeException();
            }
        }

        @Override
        public String toString() {
            return hitValueList != null ? hitValueList.toString() : hitValueSet.toString();
        }
    }

}
