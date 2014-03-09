package cc.concurrent.mango.operator;

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
import cc.concurrent.mango.util.TypeToken;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import cc.concurrent.mango.util.reflect.BeanWrapper;
import cc.concurrent.mango.util.reflect.BeanWrapperImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.lang.reflect.Array;
import java.lang.reflect.Method;
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
        rowMapper = getRowMapper(typeToken.getMappedClass());
        checkType(method.getGenericParameterTypes());
    }

    public static QueryOperator create(ASTRootNode rootNode, Method method, SQLType sqlType) {
        return new QueryOperator(rootNode, method, sqlType);
    }

    @Override
    public void checkType(Type[] methodArgTypes) {
        // 检测节点type
        TypeContext context = getTypeContext(methodArgTypes);
        rootNode.checkType(context);

        List<ASTIterableParameter> aips = rootNode.getASTIterableParameters();

        // 检测返回type
        if (!aips.isEmpty() && !isForList && !isForSet && !isForArray) { // sql中使用了in查询但返回结果不是可迭代类型
            throw new RuntimeException(""); // TODO
        }

        if (cacheDescriptor.isUseCache()) { // 使用cache
            if (aips.size() == 1) { // 在sql中有一个in语句
                String propertyName = aips.get(0).getPropertyName();
                // TODO mappedClass中必须有properName
                cacheDescriptor.setPropertyName(propertyName);
            } else if (aips.size() != 0) {
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
        Object obj = context.getPropertyValue(cacheDescriptor.getParameterName(), cacheDescriptor.getPropertyPath());
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
        context.setPropertyValue(cacheDescriptor.getParameterName(), cacheDescriptor.getPropertyPath(), missKeyObjs);
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
                dataCache.set(key, value, cacheDescriptor.getExpires());
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
            if (cacheDescriptor.isUseCache()) {
                for (T t : list) {
                    BeanWrapper beanWrapper = new BeanWrapperImpl(t);
                    Object keyObj = beanWrapper.getPropertyValue(cacheDescriptor.getPropertyName());
                    String key = getKey(cacheDescriptor.getPrefix(), keyObj);
                    dataCache.set(key, t, cacheDescriptor.getExpires());
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
            if (cacheDescriptor.isUseCache()) {
                for (T t : set) {
                    BeanWrapper beanWrapper = new BeanWrapperImpl(t);
                    Object keyObj = beanWrapper.getPropertyValue(cacheDescriptor.getPropertyName());
                    String key = getKey(cacheDescriptor.getPrefix(), keyObj);
                    dataCache.set(key, t, cacheDescriptor.getExpires());
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
            if (cacheDescriptor.isUseCache()) {
                int size = Array.getLength(array);
                for (int i = 0; i < size; i++) {
                    Object o = Array.get(array, i);
                    BeanWrapper beanWrapper = new BeanWrapperImpl(o);
                    Object keyObj = beanWrapper.getPropertyValue(cacheDescriptor.getPropertyName());
                    String key = getKey(cacheDescriptor.getPrefix(), keyObj);
                    dataCache.set(key, o, cacheDescriptor.getExpires());
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
