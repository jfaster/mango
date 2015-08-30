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

import org.jfaster.mango.annotation.Cache;
import org.jfaster.mango.annotation.CacheBy;
import org.jfaster.mango.annotation.CacheIgnored;
import org.jfaster.mango.exception.IncorrectCacheByException;
import org.jfaster.mango.exception.IncorrectDefinitionException;
import org.jfaster.mango.invoker.GetterInvokerGroup;
import org.jfaster.mango.operator.InvocationContext;
import org.jfaster.mango.operator.NameProvider;
import org.jfaster.mango.operator.ParameterContext;
import org.jfaster.mango.operator.StatsCounter;
import org.jfaster.mango.parser.ASTJDBCIterableParameter;
import org.jfaster.mango.parser.ASTJDBCParameter;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.reflect.MethodDescriptor;
import org.jfaster.mango.reflect.ParameterDescriptor;
import org.jfaster.mango.reflect.Reflection;
import org.jfaster.mango.reflect.TypeWrapper;
import org.jfaster.mango.util.Iterables;
import org.jfaster.mango.util.Strings;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author ash
 */
public class CacheDriver implements CacheBase, CacheSingleKey, CacheMultiKey {

    /**
     * 具体的缓存实现，通过{@link this#setCacheHandler(CacheHandler)}初始化
     */
    private CacheHandler cacheHandler;

    private NameProvider nameProvider;

    private StatsCounter statsCounter;

    /**
     * 缓存key前缀
     */
    private String prefix;

    /**
     * 缓存过期控制
     */
    private CacheExpire cacheExpire;

    /**
     * expire的数量
     */
    private int expireNum;

    /**
     * 是否缓存数据库中的null对象
     */
    private boolean cacheNullObject;

    /**
     * 是否缓存数据库中的空列表
     */
    private boolean cacheEmptyList;

    /**
     * cacheBy相关信息
     */
    private List<CacheByItem> cacheByItems = new ArrayList<CacheByItem>();

    /**
     * 是否使用多key缓存
     */
    private boolean useMultipleKeys;

    /**
     * "msg_id in (:1)"中的msg_id
     */
    private String propertyOfMapper;

    public CacheDriver(MethodDescriptor md, ASTRootNode rootNode, CacheHandler cacheHandler,
                           ParameterContext context, NameProvider nameProvider, StatsCounter statsCounter) {
        this.cacheHandler = cacheHandler;
        this.nameProvider = nameProvider;
        this.statsCounter = statsCounter;
        init(md, rootNode, context);
    }

    @Override
    public boolean isUseMultipleKeys() {
        return useMultipleKeys;
    }

    @Override
    public boolean isCacheNullObject() {
        return cacheNullObject;
    }

    @Override
    public boolean isCacheEmptyList() {
        return cacheEmptyList;
    }

    @Override
    public void setToCache(String key, Object value) {
        boolean success = false;
        long now = System.nanoTime();
        try {
            cacheHandler.set(key, value, cacheExpire.getExpireTime() * expireNum);
            success = true;
        } finally {
            long cost = System.nanoTime() - now;
            if (success) {
                statsCounter.recordCacheSetSuccess(cost);
            } else {
                statsCounter.recordCacheSetException(cost);
            }
        }
    }

    @Override
    public void addToCache(String key, Object value) {
        boolean success = false;
        long now = System.nanoTime();
        try {
            cacheHandler.add(key, value, cacheExpire.getExpireTime() * expireNum);
            success = true;
        } finally {
            long cost = System.nanoTime() - now;
            if (success) {
                statsCounter.recordCacheAddSuccess(cost);
            } else {
                statsCounter.recordCacheAddException(cost);
            }
        }
    }

    @Override
    public void deleteFromCache(String key) {
        boolean success = false;
        long now = System.nanoTime();
        try {
            cacheHandler.delete(key);
            success = true;
        } finally {
            long cost = System.nanoTime() - now;
            if (success) {
                statsCounter.recordCacheDeleteSuccess(cost);
            } else {
                statsCounter.recordCacheDeleteException(cost);
            }
        }
    }

    @Override
    public void batchDeleteFromCache(Set<String> keys) {
        if (keys.size() > 0) {
            boolean success = false;
            long now = System.nanoTime();
            try {
                cacheHandler.batchDelete(keys);
                success = true;
            } finally {
                long cost = System.nanoTime() - now;
                if (success) {
                    statsCounter.recordCacheBatchDeleteSuccess(cost);
                } else {
                    statsCounter.recordCacheBatchDeleteException(cost);
                }
            }
        }
    }

    @Nullable
    @Override
    public Object getFromCache(String key) {
        boolean success = false;
        long now = System.nanoTime();
        try {
            Object value = cacheHandler.get(key);
            success = true;
            return value;
        } finally {
            long cost = System.nanoTime() - now;
            if (success) {
                statsCounter.recordCacheGetSuccess(cost);
            } else {
                statsCounter.recordCacheGetException(cost);
            }
        }
    }

    @Nullable
    @Override
    public Map<String, Object> getBulkFromCache(Set<String> keys) {
        if (keys.size() > 0) {
            boolean success = false;
            long now = System.nanoTime();
            try {
                Map<String, Object> values = cacheHandler.getBulk(keys);
                success = true;
                return values;
            } finally {
                long cost = System.nanoTime() - now;
                if (success) {
                    statsCounter.recordCacheGetBulkSuccess(cost);
                } else {
                    statsCounter.recordCacheGetBulkException(cost);
                }
            }
        }
        return null;
    }

    @Override
    public String getCacheKey(InvocationContext context) {
        StringBuilder key = new StringBuilder(prefix);
        for (CacheByItem item : cacheByItems) {
            Object obj = context.getPropertyValue(item.getParameterName(), item.getInvokerGroup());
            if (obj == null) {
                throw new NullPointerException("value of " + item.getFullName() + " can't be null");
            }
            key.append("_").append(obj);
        }
        return key.toString();
    }

    @Override
    public Class<?> getOnlyCacheByClass() {
        return getOnlyCacheByItem(cacheByItems).getActualClass();
    }

    @Override
    public Set<String> getCacheKeys(InvocationContext context) {
        Iterables iterables = new Iterables(getOnlyCacheByObj(context));
        if (iterables.isEmpty()) {
            CacheByItem item = getOnlyCacheByItem(cacheByItems);
            throw new IllegalArgumentException("value of " + item.getFullName() + " can't be empty");
        }
        Set<String> keys = new HashSet<String>(iterables.size() * 2);
        for (Object obj : iterables) {
            String key = getCacheKey(obj);
            keys.add(key);
        }
        return keys;
    }

    @Override
    public String getCacheKey(Object obj) {
        return prefix + "_" + obj;
    }

    @Override
    public Object getOnlyCacheByObj(InvocationContext context) {
        CacheByItem item = getOnlyCacheByItem(cacheByItems);
        Object obj = context.getPropertyValue(item.getParameterName(), item.getInvokerGroup());
        if (obj == null) {
            throw new NullPointerException("value of " + item.getFullName() + " can't be null");
        }
        return obj;
    }

    @Override
    public void setOnlyCacheByObj(InvocationContext context, Object obj) {
        CacheByItem item = getOnlyCacheByItem(cacheByItems);
        context.setPropertyValue(item.getParameterName(), item.getInvokerGroup(), obj);
    }

    @Override
    public String getPropertyOfMapper() {
        return propertyOfMapper;
    }

    private void init(MethodDescriptor md, ASTRootNode rootNode, ParameterContext context) {
        for (ParameterDescriptor pd : md.getParameterDescriptors()) {
            CacheBy cacheByAnno = pd.getAnnotation(CacheBy.class);
            if (cacheByAnno != null) {
                String parameterName = nameProvider.getParameterName(pd.getPosition());
                String propertyPaths = cacheByAnno.value();
                for (String propertyPath : propertyPaths.split(",")) {
                    propertyPath = propertyPath.trim();
                    GetterInvokerGroup invokerGroup = context.getInvokerGroup(parameterName, propertyPath);
                    Type cacheByType = invokerGroup.getFinalType();
                    TypeWrapper tw = new TypeWrapper(cacheByType);
                    cacheByItems.add(new CacheByItem(parameterName, propertyPath, tw.getMappedClass(), invokerGroup));
                    useMultipleKeys = useMultipleKeys || tw.isIterable();
                }
            }
        }
        int cacheByNum = cacheByItems.size();
        if (useMultipleKeys && cacheByNum > 1) { // 当@CacheBy修饰in语句时，只能有1个@CacheBy
            throw new IncorrectCacheByException("when @CacheBy modification interable parameter, " +
                    "there can be only one @CacheBy");
        }

        Cache cacheAnno = md.getAnnotation(Cache.class);
        CacheIgnored cacheIgnoredAnno = md.getAnnotation(CacheIgnored.class);
        if (cacheAnno != null) { // dao类使用cache
            if (cacheIgnoredAnno == null) { // method不禁用cache
                if (cacheByNum == 0) {
                    throw new IllegalStateException("if use cache, each method " +
                            "expected one or more @CacheBy annotation on parameter " +
                            "but found 0");
                }
                prefix = cacheAnno.prefix();
                cacheExpire = Reflection.instantiateClass(cacheAnno.expire());
                expireNum = cacheAnno.num();
                cacheNullObject = cacheAnno.cacheNullObject();
                cacheEmptyList = cacheAnno.cacheEmptyList();
                checkCacheBy(rootNode, cacheByItems);
            } else {
                if (cacheByNum > 0) {
                    throw new IncorrectDefinitionException("if @CacheIgnored is on method, " +
                            "@CacheBy can not on method's parameter");
                }
            }
        } else {
            if (cacheByNum > 0) {
                throw new IncorrectDefinitionException("if @Cache is not defined, " +
                        "@CacheBy can not on method's parameter");
            }
            if (cacheIgnoredAnno != null) {
                throw new IncorrectDefinitionException("if @Cache is not defined, " +
                        "@CacheIgnored can not on method");
            }
        }

        if (useMultipleKeys) {
            CacheByItem cacheByItem = getOnlyCacheByItem(cacheByItems);
            for (ASTJDBCIterableParameter jip : rootNode.getJDBCIterableParameters()) {
                if (jip.getParameterName().equals(cacheByItem.getParameterName())
                        && jip.getPropertyPath().equals(cacheByItem.getPropertyPath())) {
                    propertyOfMapper = jip.getPropertyOfMapper();
                    break;
                }
            }
        }
    }

    private static CacheByItem getOnlyCacheByItem(List<CacheByItem> cacheByItems) {
        if (cacheByItems.size() != 1) {
            throw new IllegalStateException("size of cacheByItems expected 1 but " + cacheByItems.size());
        }
        return cacheByItems.get(0);
    }

    /**
     * 检测{@link CacheBy}定位到的参数db中是否有用到，如果db中没有用到，
     * 则抛出{@link org.jfaster.mango.exception.IncorrectCacheByException}
     */
    private static void checkCacheBy(ASTRootNode rootNode, List<CacheByItem> cacheByItems) {
        List<ASTJDBCParameter> jps = rootNode.getJDBCParameters();
        for (CacheByItem cacheByItem : cacheByItems) {
            String parameterName = cacheByItem.getParameterName();
            String propertyPath = cacheByItem.getPropertyPath();
            boolean pass = false;
            for (ASTJDBCParameter jp : jps) {
                if (jp.getParameterName().equals(parameterName) &&
                        jp.getPropertyPath().equals(propertyPath)) {
                    pass = true;
                    break;
                }
            }
            List<ASTJDBCIterableParameter> jips = rootNode.getJDBCIterableParameters();
            for (ASTJDBCIterableParameter jip : jips) {
                if (jip.getParameterName().equals(parameterName) &&
                        jip.getPropertyPath().equals(propertyPath)) {
                    pass = true;
                    break;
                }
            }
            if (!pass) {
                throw new IncorrectCacheByException("CacheBy " +
                        cacheByItem.getFullName() + " can't match any db parameter");
            }
        }
    }

    private static class CacheByItem {

        private final String parameterName;

        private final String propertyPath;

        private final Class<?> actualClass;

        private final GetterInvokerGroup invokerGroup;

        public CacheByItem(String parameterName, String propertyPath, Class<?> actualClass, GetterInvokerGroup invokerGroup) {
            this.parameterName = parameterName;
            this.propertyPath = propertyPath;
            this.actualClass = actualClass;
            this.invokerGroup = invokerGroup;
        }

        public String getParameterName() {
            return parameterName;
        }

        public String getPropertyPath() {
            return propertyPath;
        }

        private Class<?> getActualClass() {
            return actualClass;
        }

        private GetterInvokerGroup getInvokerGroup() {
            return invokerGroup;
        }

        public String getFullName() {
            return Strings.getFullName(parameterName, propertyPath);
        }

    }

}
