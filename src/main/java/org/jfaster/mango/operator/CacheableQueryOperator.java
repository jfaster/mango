package org.jfaster.mango.operator;

import org.jfaster.mango.exception.IncorrectSqlException;
import org.jfaster.mango.exception.NotReadablePropertyException;
import org.jfaster.mango.exception.UnreachableCodeException;
import org.jfaster.mango.operator.driver.CacheableOperatorDriver;
import org.jfaster.mango.parser.node.ASTJDBCIterableParameter;
import org.jfaster.mango.parser.node.ASTRootNode;
import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.util.Iterables;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;
import org.jfaster.mango.util.reflect.BeanInfoCache;
import org.jfaster.mango.util.reflect.Beans;

import java.lang.reflect.Method;
import java.util.*;

/**
 * @author ash
 */
public class CacheableQueryOperator extends QueryOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(CacheableUpdateOperator.class);

    private CacheableOperatorDriver driver;

    private String interableProperty = driver.getInterableProperty();

    protected CacheableQueryOperator(ASTRootNode rootNode, CacheableOperatorDriver driver, Method method, StatsCounter statsCounter) {
        super(rootNode, driver, method, statsCounter);
        this.driver = driver;

        List<ASTJDBCIterableParameter> jips = rootNode.getJDBCIterableParameters();
        if (jips.size() > 1) {
            throw new IncorrectSqlException("if use cache, sql's in clause expected less than or equal 1 but " +
                    jips.size()); // sql中不能有多个in语句
        }

        if (driver.isUseMultipleKeys()) {
            interableProperty = driver.getInterableProperty();
            Method readMethod = BeanInfoCache.getReadMethod(mappedClass, interableProperty);
            if (readMethod == null) {
                // 如果使用cache并且sql中有一个in语句，mappedClass必须含有特定属性，必须a in (...)，则mappedClass必须含有a属性
                throw new NotReadablePropertyException("if use cache and sql has one in clause, property "
                        + interableProperty + " of " + mappedClass + " expected readable but not");
            }
        }
    }

    @Override
    public Object execute(Object[] methodArgs) {
        RuntimeContext context = driver.buildRuntimeContext(methodArgs);
        return driver.isUseMultipleKeys() ?
                multipleKeysCache(context, rowMapper.getMappedClass(), driver.getSuffixClass()) :
                singleKeyCache(context);
    }

    private <T, U> Object multipleKeysCache(RuntimeContext context, Class<T> mappedClass, Class<U> suffixClass) {
        boolean isDebugEnabled = logger.isDebugEnabled();
        Set<String> keys = driver.getCacheKeys(context);
        Map<String, Object> cacheResults = driver.getBulkFromCache(keys);
        AddableObject<T> addableObj = new AddableObject<T>(keys.size(), mappedClass);
        int hitCapacity = cacheResults != null ? cacheResults.size() : 0;
        List<U> hitSuffix = new ArrayList<U>(hitCapacity); // 用于debug
        Set<U> missSuffix = new HashSet<U>((keys.size() - hitCapacity) * 2);
        for (Object suffix : new Iterables(driver.getSuffixObj(context))) {
            String key = driver.getCacheKey(suffix);
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
            driver.setSuffixObj(context, missSuffix);
            Object dbValues = execute(context);
            for (Object dbValue : new Iterables(dbValues)) {
                // db数据添加入结果
                addableObj.add(mappedClass.cast(dbValue));
                // 添加入缓存
                Object suffix = Beans.getPropertyValue(dbValue, interableProperty, mappedClass);
                String key = driver.getCacheKey(suffix);
                driver.setToCache(key, dbValue);
            }
        }
        return addableObj.getReturn();
    }

    private Object singleKeyCache(RuntimeContext context) {
        String key = driver.getCacheKey(context);
        Object value = driver.getFromCache(key);
        if (value == null) {
            if (logger.isDebugEnabled()) {
                logger.debug("cache miss #key＝{}", key);
            }
            value = execute(context);
            if (value != null) {
                if (logger.isDebugEnabled()) {
                    logger.debug("cache set #key={} #value={}", key, value);
                }
                driver.setToCache(key, value);
            }
        } else {
            if (logger.isDebugEnabled()) {
                logger.debug("cache hit #key={} #value={}", key, value);
            }
        }
        return value;
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
