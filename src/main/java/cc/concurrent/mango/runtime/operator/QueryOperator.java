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
import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;
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
import java.util.*;

/**
 * 处理所有的查询操作
 *
 * @author ash
 */
public class QueryOperator extends CacheableOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(QueryOperator.class);

    private ASTRootNode rootNode;
    private RowMapper<?> rowMapper;
    private Class<?> mappedClass;
    private boolean isForList;
    private boolean isForSet;
    private boolean isForArray;
    private String interableProperty; // "a in (:1)"中的a


    private QueryOperator(ASTRootNode rootNode, Method method, SQLType sqlType) {
        super(method, sqlType);
        init(rootNode, method);
    }

    private void init(ASTRootNode rootNode, Method method) {
        this.rootNode = rootNode;
        TypeToken typeToken = new TypeToken(method.getGenericReturnType());
        isForList = typeToken.isList();
        isForSet = typeToken.isSet();
        isForArray = typeToken.isArray();
        mappedClass = typeToken.getMappedClass();
        rowMapper = getRowMapper(mappedClass);

        TypeContext context = buildTypeContext(method.getGenericParameterTypes());
        rootNode.checkType(context); // 检测sql中的参数是否和方法上的参数匹配
        checkCacheBy(rootNode); // 如果使用cache，检测cache参数

        List<ASTIterableParameter> aips = rootNode.getASTIterableParameters();
        if (!aips.isEmpty() && !isForList && !isForSet && !isForArray) {
            throw new IncorrectReturnTypeException("if sql has in clause, return type " +
                    "expected array or implementations of java.util.List or implementations of java.util.Set " +
                    "but " + method.getGenericReturnType()); // sql中使用了in查询，返回参数必须可迭代
        }
        if (isUseCache()) {
            if (aips.size() == 1) {
                interableProperty = aips.get(0).getInterableProperty();
                Method readMethod = BeanInfoCache.getReadMethod(mappedClass, interableProperty);
                if (readMethod == null) {
                    // 如果使用cache并且sql中有一个in语句，mappedClass必须含有特定属性，必须a in (...)，则mappedClass必须含有a属性
                    throw new NotReadablePropertyException("if use cache and sql has one in clause, property "
                            + interableProperty + " of " + mappedClass + " expected readable but not");
                }
            } else if (aips.size() != 0) {
                throw new IncorrectSqlException("if use cache, sql's in clause expected less than or equal 1 but "
                        + aips.size()); // 如果使用cache，sql中的in语句不能超过1
            }
        }
    }

    public static QueryOperator create(ASTRootNode rootNode, Method method, SQLType sqlType) {
        return new QueryOperator(rootNode, method, sqlType);
    }


    @Override
    public Object execute(Object[] methodArgs) {
        RuntimeContext context = buildRuntimeContext(methodArgs);
        if (isUseCache()) { // 先使用缓存，再使用db
            return executeFromCache(context);
        } else { // 直接使用db
            return executeFromDb(context, rowMapper);
        }
    }

    private Object executeFromCache(RuntimeContext context) {
        Object obj = getCacheKeyObj(context);
        Iterables iterables = new Iterables(obj);
        if (iterables.isIterable()) { // 多个key
            Set<String> keys = new HashSet<String>();
            Class<?> keyObjClass = null;
            for (Object keyObj : iterables) {
                String key = getKey(keyObj);
                keys.add(key);
                if (keyObjClass == null) {
                    keyObjClass = keyObj.getClass();
                }
            }
            return multipleKeysCache(context, iterables, keys, rowMapper.getMappedClass(), keyObjClass);
        } else { // 单个key
            String key = getKey(obj);
            return singleKeyCache(context, key);
        }
    }

    private <T, U> Object multipleKeysCache(RuntimeContext context, Iterables keyObjIterables, Set<String> keys,
                                            Class<T> valueClass, Class<U> keyObjClass) {
        boolean isDebugEnabled = logger.isDebugEnabled();

        Map<String, Object> map = getBulkFromCache(keys);
        int initialCapacity = Math.max(1, map != null ? map.size() : 0);
        AddableObject<T> addableObj = new AddableObject<T>(initialCapacity, valueClass);
        List<U> hitKeyObjs = new ArrayList<U>(); // 用于debug
        Set<U> missKeyObjs = new HashSet<U>();
        for (Object keyObj : keyObjIterables) {
            String key = getKey(keyObj);
            Object value = map != null ? map.get(key) : null;
            if (value == null) {
                missKeyObjs.add(keyObjClass.cast(keyObj));
            } else {
                addableObj.add(valueClass.cast(value));
                if (isDebugEnabled) {
                    hitKeyObjs.add(keyObjClass.cast(keyObj));
                }
            }
        }
        if (isDebugEnabled) {
            logger.debug("cache hit #keys={} #values={}", hitKeyObjs, addableObj);
            logger.debug("cache miss #keys={}", missKeyObjs);
        }
        statsCounter.recordHits(hitKeyObjs.size());
        statsCounter.recordMisses(missKeyObjs.size());
        if (!missKeyObjs.isEmpty()) { // 有key没有命中
            context.setPropertyValue(getCacheParameterName(), getCachePropertyPath(), missKeyObjs);
            Object dbValues = executeFromDb(context, rowMapper);
            for (Object dbValue : new Iterables(dbValues)) {
                // db数据添加入结果
                addableObj.add(valueClass.cast(dbValue));

                // 添加入缓存
                Object keyObj = BeanUtil.getPropertyValue(dbValue, interableProperty, mappedClass);
                String key = getKey(keyObj);
                setToCache(key, dbValue);
            }
        }
        return addableObj.getReturn();
    }

    private Object singleKeyCache(RuntimeContext context, String key) {
        Object value = getFromCache(key);
        if (value == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("cache miss #key＝{}", key);
            }
            statsCounter.recordHits(1);
            value = executeFromDb(context, rowMapper);
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
            statsCounter.recordMisses(1);
        }
        return value;
    }

    private <T> Object executeFromDb(RuntimeContext context, RowMapper<T> rowMapper) {
        ParsedSql parsedSql = rootNode.buildSqlAndArgs(context);
        String sql = parsedSql.getSql();
        Object[] args = parsedSql.getArgs();
        if (logger.isDebugEnabled()) {
            logger.debug("{} #args={}", sql, args);
        }
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
        if (logger.isDebugEnabled()) {
            logger.debug("{} #result={}", sql, r);
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
