package cc.concurrent.mango.operator;

import cc.concurrent.mango.exception.structure.IncorrectReturnTypeException;
import cc.concurrent.mango.jdbc.BeanPropertyRowMapper;
import cc.concurrent.mango.jdbc.JdbcUtils;
import cc.concurrent.mango.jdbc.RowMapper;
import cc.concurrent.mango.jdbc.SingleColumnRowMapper;
import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.RuntimeContextImpl;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.runtime.parser.ASTIterableParameter;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.Iterables;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 处理所有的查询操作
 *
 * @author ash
 */
public class QueryOperator extends AbstractOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(QueryOperator.class);

    private ASTRootNode rootNode;
    private RowMapper<?> rowMapper;
    private boolean isForList;
    private boolean isForSet;
    private boolean isForArray;

    private QueryOperator(ASTRootNode rootNode, Method method) {
        this.rootNode = rootNode;
        init(method);
    }

    private void init(Method method) {
        Class<?> mappedClass = null;
        Type genericReturnType = method.getGenericReturnType();
        if (genericReturnType instanceof ParameterizedType) { // 参数化类型
            ParameterizedType parameterizedType = (ParameterizedType) genericReturnType;
            Type rawType = parameterizedType.getRawType();
            if (rawType instanceof Class) {
                Class<?> rawClass = (Class<?>) rawType;
                if (List.class.equals(rawClass)) {
                    isForList = true;
                    Type typeArgument = parameterizedType.getActualTypeArguments()[0];
                    if (typeArgument instanceof Class) {
                        mappedClass = (Class<?>) typeArgument;
                    }
                } else if (Set.class.equals(rawClass)) {
                    isForSet = true;
                    Type typeArgument = parameterizedType.getActualTypeArguments()[0];
                    if (typeArgument instanceof Class) {
                        mappedClass = (Class<?>) typeArgument;
                    }
                }
            }
        } else if (genericReturnType instanceof Class) { // 没有参数化
            Class<?> clazz = (Class<?>) genericReturnType;
            if (clazz.isArray()) { // 数组
                isForArray = true;
                mappedClass = clazz.getComponentType();
            } else { // 普通类
                mappedClass = clazz;
            }
        }
        if (mappedClass == null) {
            throw new IncorrectReturnTypeException("return type " + method.getGenericReturnType() + " is error");
        }
        rowMapper = getRowMapper(mappedClass);
        checkType(method.getParameterTypes());
    }

    public static QueryOperator create(ASTRootNode rootNode, Method method) {
        return new QueryOperator(rootNode, method);
    }

    @Override
    public void checkType(Class<?>[] methodArgTypes) {
        // 检测节点type
        TypeContext context = getTypeContext(methodArgTypes);
        rootNode.checkType(context);

        List<ASTIterableParameter> aips = rootNode.getASTIterableParameters();

        // 检测返回type
        if (!aips.isEmpty() && !isForList && !isForSet && !isForArray) { // sql中使用了in查询但返回结果不是可迭代类型
            // TODO
            throw new RuntimeException("");
        }

        if (cacheDescriptor.isUseCache()) { // 使用cache
            if (aips.size() == 1) { // 在sql中只能存在一个in语句
                String propertyName = aips.get(0).getPropertyName();
                // TODO mappedClass中必须有properName
                cacheDescriptor.setPropertyName(propertyName);
            } else {
                // TODO
                throw new RuntimeException("");
            }
        }
    }

    @Override
    public Object execute(Object[] methodArgs) {
        Map<String, Object> parameters = Maps.newHashMap();
        for (int i = 0; i < methodArgs.length; i++) {
            parameters.put(String.valueOf(i + 1), methodArgs[i]);
        }
        RuntimeContext context = new RuntimeContextImpl(parameters);
        if (cacheDescriptor.isUseCache()) { // 先使用缓存，再使用db
            return executeFromCache(context);
        } else { // 直接使用db
            return executeFromDb(context, rowMapper, null);
        }
    }

    private Object executeFromCache(RuntimeContext context) {
        Object obj = context.getPropertyValue(cacheDescriptor.getBeanName(), cacheDescriptor.getPropertyName());
        Iterables iterables = new Iterables(obj);
        if (iterables.isIterable()) { // 多个key
            Set<String> keys = Sets.newHashSet();
            Class<?> keyObjClass = null;
            for (Object keyObj : iterables) {
                String key = getKey(cacheDescriptor.getPrefix(), keyObj);
                keys.add(key);
                if (keyObjClass == null) {
                    keyObjClass = keyObj.getClass();
                }
            }
            return multipleKeysCache(context, iterables, keys, rowMapper.getMappedClass(), keyObjClass);
        } else { // 单个key
            String key = cacheDescriptor.getPrefix() + obj;
            return singleKeyCache(context, key);
        }
    }

    private <T, U> Object multipleKeysCache(RuntimeContext context, Iterables iterables, Set<String> keys,
                                            Class<T> valueClass, Class<U> keyObjClass) {
        boolean isDebugEnabled = logger.isDebugEnabled();

        Map<String, Object> map = dataCache.getBulk(keys);
        List<T> hitValues = Lists.newArrayList();
        List<U> hitKeyObjs = Lists.newArrayList(); // 用于debug
        Set<U> missKeyObjs = Sets.newHashSet();
        for (Object keyObj : iterables) {
            String key = getKey(cacheDescriptor.getPrefix(), keyObj);
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
                return Lists.newArrayList(hitValues);
            } else if (isForSet) {
                return hitValues;
            } else if (isForArray) {
                Object array = Array.newInstance(valueClass, hitValues.size());
                int i = 0;
                for (T hitValue : hitValues) {
                    Array.set(array, i++, hitValue);
                }
                return array;
            } else {
                // TODO exception
            }
        }
        context.setPropertyValue(cacheDescriptor.getBeanName(), cacheDescriptor.getPropertyName(), missKeyObjs);
        return executeFromDb(context, rowMapper, hitValues);
    }

    private Object singleKeyCache(RuntimeContext context, String key) {
        Object value = dataCache.get(key);
        if (value == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("cache miss #key＝{}", key);
            }
            value = executeFromDb(context, rowMapper, null);
            if (value != null) {
                dataCache.set(key, value);
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
            List<T> list = jdbcTemplate.queryForList(sql, args, rowMapper);
            if (logger.isDebugEnabled()) {
                logger.debug("{} #result={}", sql, list);
            }
            if (hitValues != null && !hitValues.isEmpty()) {
                for (Object hitValue : hitValues) {
                    list.add(valueClass.cast(hitValue));
                }
            }
            return list;
        } else if (isForSet) {
            Set<T> set = jdbcTemplate.queryForSet(sql, args, rowMapper);
            if (logger.isDebugEnabled()) {
                logger.debug("{} #result={}", sql, set);
            }
            if (hitValues != null && !hitValues.isEmpty()) {
                for (Object hitValue : hitValues) {
                    set.add(valueClass.cast(hitValue));
                }
            }
            return set;
        } else if (isForArray) {
            Object array = jdbcTemplate.queryForArray(sql, args, rowMapper);
            if (logger.isDebugEnabled()) {
                logger.debug("{} #result={}", sql, array);
            }
            if (hitValues == null || hitValues.isEmpty()) {
                return array;
            }
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
            Object r = jdbcTemplate.queryForObject(sql, args, rowMapper);
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
