package cc.concurrent.mango.operator;

import cc.concurrent.mango.jdbc.RowMapper;
import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.RuntimeContextImpl;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.Iterables;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import java.lang.reflect.Array;
import java.util.*;

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

    public QueryOperator(ASTRootNode rootNode, RowMapper<?> rowMapper, boolean isForList, boolean isForSet, boolean isForArray) {
        this.rootNode = rootNode;
        this.rowMapper = rowMapper;
        this.isForList = isForList;
        this.isForSet = isForSet;
        this.isForArray = isForArray;
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
            Class<?> keyClass = null;
            for (Object keyObj : iterables) {
                String key = cacheDescriptor.getPrefix() + keyObj;
                keys.add(key);
                if (keyClass == null) {
                    keyClass = keyObj.getClass();
                }
            }
            return multipleKeysCache(context, iterables, keys, rowMapper.getMappedClass(), keyClass);
        } else { // 单个key
            String key = cacheDescriptor.getPrefix() + obj;
            return singleKeyCache(context, key);
        }
    }

    private <T, U> Object multipleKeysCache(RuntimeContext context, Iterables iterables, Set<String> keys,
                                            Class<T> mappedClass, Class<U> keyClass) {
        Map<String, Object> map = dataCache.getBulk(keys);
        List<T> hitValueObjs = Lists.newArrayList();
        Set<U> missKeyObjs = Sets.newHashSet();
        for (Object keyObj : iterables) {
            String key = cacheDescriptor.getPrefix() + keyObj;
            Object value = map != null ? map.get(key) : null;
            if (value == null) {
                missKeyObjs.add(keyClass.cast(keyObj));
            } else {
                hitValueObjs.add(mappedClass.cast(value));
            }
        }
        if (missKeyObjs.isEmpty()) { // cache中命中
            if (isForList) {
                return hitValueObjs;
            } else if (isForSet) {
                return Sets.newHashSet(hitValueObjs);
            } else if (isForArray) {
                Object array = Array.newInstance(mappedClass, hitValueObjs.size());
                for (int i = 0; i < hitValueObjs.size(); i++) {
                    Array.set(array, i, hitValueObjs.get(i));
                }
                return array;
            } else {
                // TODO exception
            }
        }
        context.setPropertyValue(cacheDescriptor.getBeanName(), cacheDescriptor.getPropertyName(), missKeyObjs);
        return executeFromDb(context, rowMapper, hitValueObjs);
    }

    private Object singleKeyCache(RuntimeContext context, String key) {
        Object value = dataCache.get(key);
        if (value == null) {
            value = executeFromDb(context, rowMapper, null);
            if (value != null) {
                dataCache.set(key, value);
            }
        }
        return value;
    }

    private <T> Object executeFromDb(RuntimeContext context, RowMapper<T> rowMapper, List<?> hitValueObjs) {
        ParsedSql parsedSql = rootNode.getSqlAndArgs(context);
        String sql = parsedSql.getSql();
        Object[] args = parsedSql.getArgs();
        if (logger.isDebugEnabled()) {
            logger.debug(Objects.toStringHelper("QueryOperator").add("sql", sql).add("args", Arrays.toString(args)).toString());
        }
        Class<T> mappedClass = rowMapper.getMappedClass();
        if (isForList) {
            List<T> list = jdbcTemplate.queryForList(sql, args, rowMapper);
            if (hitValueObjs != null && !hitValueObjs.isEmpty()) {
                for (Object hitValueObj : hitValueObjs) {
                    list.add(mappedClass.cast(hitValueObj));
                }
            }
            return list;
        } else if (isForSet) {
            Set<T> set = jdbcTemplate.queryForSet(sql, args, rowMapper);
            if (hitValueObjs != null && !hitValueObjs.isEmpty()) {
                for (Object hitValueObj : hitValueObjs) {
                    set.add(mappedClass.cast(hitValueObj));
                }
            }
            return set;
        } else if (isForArray) {
            Object array = jdbcTemplate.queryForArray(sql, args, rowMapper);
            if (hitValueObjs == null || hitValueObjs.isEmpty()) {
                return array;
            }
            int cacheSize = hitValueObjs.size();
            int dbSize = Array.getLength(array);
            int size = cacheSize + dbSize;
            Object r = Array.newInstance(mappedClass, size);
            for (int i = 0; i < size; i++) {
                Object value = i <  cacheSize ? hitValueObjs.get(i) : Array.get(array, i - cacheSize);
                Array.set(r, i, value);
            }
            return r;
        } else {
            return jdbcTemplate.queryForObject(sql, args, rowMapper);
        }
    }

}
