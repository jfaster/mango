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
import cc.concurrent.mango.util.Iterables;
import cc.concurrent.mango.util.TypeToken;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import cc.concurrent.mango.util.reflect.BeanInfoCache;
import cc.concurrent.mango.util.reflect.BeanUtil;

import java.lang.reflect.Array;
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
            throw new IncorrectReturnTypeException("if sql has in clause, return type expected iterable but "
                    + method.getGenericReturnType()); // sql中使用了in查询，返回参数必须可迭代
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
            return executeFromDb(context, rowMapper, null);
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

    private <T, U> Object multipleKeysCache(RuntimeContext context, Iterables iterables, Set<String> keys,
                                            Class<T> valueClass, Class<U> keyObjClass) {
        boolean isDebugEnabled = logger.isDebugEnabled();

        Map<String, Object> map = getBulkFromCache(keys);
        List<T> hitValues = new ArrayList<T>();
        List<U> hitKeyObjs = new ArrayList<U>(); // 用于debug
        Set<U> missKeyObjs = new HashSet<U>();
        for (Object keyObj : iterables) {
            String key = getKey(keyObj);
            Object value = map != null ? map.get(key) : null;
            if (value == null) {
                missKeyObjs.add(keyObjClass.cast(keyObj));
            } else {
                hitValues.add(valueClass.cast(value));
                if (isDebugEnabled) {
                    hitKeyObjs.add(keyObjClass.cast(keyObj));
                }
            }
        }
        if (isDebugEnabled) {
            logger.debug("cache hit #keys={} #values={}", hitKeyObjs, hitValues);
            logger.debug("cache miss #keys={}", missKeyObjs);
        }
        if (missKeyObjs.isEmpty()) { // 所有的key全部命中
            if (isForList) {
                return hitValues;
            } else if (isForSet) {
                return new HashSet<T>(hitValues); // TODO 添加测试用例
            } else if (isForArray) {
                Object array = Array.newInstance(valueClass, hitValues.size());
                int i = 0;
                for (T hitValue : hitValues) {
                    Array.set(array, i++, hitValue);
                }
                return array;
            } else {
                throw new UnreachableCodeException();
            }
        }
        context.setPropertyValue(getCacheParameterName(), getCachePropertyPath(), missKeyObjs);
        return executeFromDb(context, rowMapper, hitValues);
    }

    private Object singleKeyCache(RuntimeContext context, String key) {
        Object value = getFromCache(key);
        if (value == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("cache miss #key＝{}", key);
            }
            value = executeFromDb(context, rowMapper, null);
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

    private <T> Object executeFromDb(RuntimeContext context, RowMapper<T> rowMapper, List<?> hitValues) {
        ParsedSql parsedSql = rootNode.buildSqlAndArgs(context);
        String sql = parsedSql.getSql();
        Object[] args = parsedSql.getArgs();
        if (logger.isDebugEnabled()) {
            logger.debug("{} #args={}", sql, args);
        }
        Class<T> valueClass = rowMapper.getMappedClass();
        if (isForList) {
            List<T> list = jdbcTemplate.queryForList(getDataSource(), sql, args, rowMapper);
            if (logger.isDebugEnabled()) {
                logger.debug("{} #result={}", sql, list);
            }
            if (isUseCache()) {
                for (T t : list) {
                    Object keyObj = BeanUtil.getPropertyValue(t, interableProperty, mappedClass);
                    String key = getKey(keyObj);
                    setToCache(key, t);
                }
            }
            if (hitValues != null && !hitValues.isEmpty()) { // 拼装cache与db的结果
                for (Object hitValue : hitValues) {
                    list.add(valueClass.cast(hitValue));
                }
            }
            return list;
        } else if (isForSet) {
            Set<T> set = jdbcTemplate.queryForSet(getDataSource(), sql, args, rowMapper);
            if (logger.isDebugEnabled()) {
                logger.debug("{} #result={}", sql, set);
            }
            if (isUseCache()) {
                for (T t : set) {
                    Object keyObj = BeanUtil.getPropertyValue(t, interableProperty, mappedClass);
                    String key = getKey(keyObj);
                    setToCache(key, t);
                }
            }
            if (hitValues != null && !hitValues.isEmpty()) { // 拼装cache与db的结果
                for (Object hitValue : hitValues) {
                    set.add(valueClass.cast(hitValue));
                }
            }
            return set;
        } else if (isForArray) {
            Object array = jdbcTemplate.queryForArray(getDataSource(), sql, args, rowMapper);
            if (logger.isDebugEnabled()) {
                logger.debug("{} #result={}", sql, array);
            }
            if (isUseCache()) {
                int size = Array.getLength(array);
                for (int i = 0; i < size; i++) {
                    Object o = Array.get(array, i);
                    Object keyObj = BeanUtil.getPropertyValue(o, interableProperty, mappedClass);
                    String key = getKey(keyObj);
                    setToCache(key, o);
                }
            }
            if (hitValues == null || hitValues.isEmpty()) {
                return array;
            }

            // 拼装cache与db的结果
            int cacheSize = hitValues.size();
            int dbSize = Array.getLength(array);
            int size = cacheSize + dbSize;
            Object r = Array.newInstance(valueClass, size);
            int i = 0;
            for (Object hitValue : hitValues) {
                Object value = i <  cacheSize ? hitValue : Array.get(array, i - cacheSize);
                Array.set(r, i, value);
                i++;
            }
            return r;
        } else {
            Object r = jdbcTemplate.queryForObject(getDataSource(), sql, args, rowMapper);
            if (logger.isDebugEnabled()) {
                logger.debug("{} #result={}", sql, r);
            }
            return r;
        }
    }

    private static <T> RowMapper<T> getRowMapper(Class<T> clazz) {
        return JdbcUtils.isSingleColumnClass(clazz) ?
                new SingleColumnRowMapper<T>(clazz) :
                new BeanPropertyRowMapper<T>(clazz);
    }

}
