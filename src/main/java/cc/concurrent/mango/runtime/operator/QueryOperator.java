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

package cc.concurrent.mango.runtime.operator;

import cc.concurrent.mango.exception.IncorrectReturnTypeException;
import cc.concurrent.mango.exception.IncorrectSqlException;
import cc.concurrent.mango.exception.NotReadablePropertyException;
import cc.concurrent.mango.exception.UnreachableCodeException;
import cc.concurrent.mango.jdbc.BeanPropertyRowMapper;
import cc.concurrent.mango.jdbc.JdbcUtils;
import cc.concurrent.mango.jdbc.RowMapper;
import cc.concurrent.mango.jdbc.SingleColumnRowMapper;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.parser.ASTIterableParameter;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.ArrayUtil;
import cc.concurrent.mango.util.Iterables;
import cc.concurrent.mango.util.TypeToken;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import cc.concurrent.mango.util.reflect.BeanInfoCache;
import cc.concurrent.mango.util.reflect.BeanUtil;

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

    public QueryOperator(ASTRootNode rootNode, Method method, SQLType sqlType) {
        super(rootNode, method, sqlType);
        init(rootNode, method);
    }

    private void init(ASTRootNode rootNode, Method method) {
        TypeToken typeToken = new TypeToken(method.getGenericReturnType());
        isForList = typeToken.isList();
        isForSet = typeToken.isSet();
        isForArray = typeToken.isArray();
        Class<?> mappedClass = typeToken.getMappedClass();
        rowMapper = getRowMapper(mappedClass);

        List<ASTIterableParameter> ips = rootNode.getIterableParameters();
        if (!ips.isEmpty() && !isForList && !isForSet && !isForArray) {
            throw new IncorrectReturnTypeException("if sql has in clause, return type " +
                    "expected array or implementations of java.util.List or implementations of java.util.Set " +
                    "but " + method.getGenericReturnType()); // sql中使用了in查询，返回参数必须可迭代
        }
        if (isUseCache()) {
            if (ips.size() == 1) {
                interableProperty = ips.get(0).getInterableProperty();
                Method readMethod = BeanInfoCache.getReadMethod(mappedClass, interableProperty);
                if (readMethod == null) {
                    // 如果使用cache并且sql中有一个in语句，mappedClass必须含有特定属性，必须a in (...)，则mappedClass必须含有a属性
                    throw new NotReadablePropertyException("if use cache and sql has one in clause, property "
                            + interableProperty + " of " + mappedClass + " expected readable but not");
                }
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
            List<ASTIterableParameter> ips = rootNode.getIterableParameters();
            if (ips.size() > 1) {
                throw new IncorrectSqlException("if use cache, sql's in clause expected less than or equal 1 but " +
                        ips.size()); // sql中不能有多个in语句
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
                Object suffix = BeanUtil.getPropertyValue(dbValue, interableProperty, mappedClass);
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
                setToCache(key, value);
                if (logger.isDebugEnabled()) {
                    logger.debug("cache set #key={} #value={}", key, value);
                }
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("cache hit #key={} #value={}", key, value);
            }
        }
        return value;
    }

    private Object executeFromDb(RuntimeContext context) {
        String sql = rootNode.getSql(context);
        Object[] args = rootNode.getArgs(context);
        Object r = null;
        long now = System.nanoTime();
        try {
            if (isForList) {
                r = jdbcTemplate.queryForList(getDataSource(), sql, args, rowMapper);
            } else if (isForSet) {
                r = jdbcTemplate.queryForSet(getDataSource(), sql, args, rowMapper);
            } else if (isForArray) {
                r= jdbcTemplate.queryForArray(getDataSource(), sql, args, rowMapper);
            } else {
                r = jdbcTemplate.queryForObject(getDataSource(), sql, args, rowMapper);
            }
        } finally {
            long cost = System.nanoTime() - now;
            if (r != null) {
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
                return ArrayUtil.toArray(hitValueList, valueClass);
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
