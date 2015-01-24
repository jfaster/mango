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

import org.jfaster.mango.annotation.Cache;
import org.jfaster.mango.annotation.CacheBy;
import org.jfaster.mango.annotation.CacheIgnored;
import org.jfaster.mango.cache.CacheExpire;
import org.jfaster.mango.cache.CacheHandler;
import org.jfaster.mango.exception.IncorrectCacheByException;
import org.jfaster.mango.exception.IncorrectDefinitionException;
import org.jfaster.mango.invoker.GetterInvoker;
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
public class CacheDriverImpl implements CacheDriver {

    /**
     * 具体的缓存实现，通过{@link this#setCacheHandler(org.jfaster.mango.cache.CacheHandler)}初始化
     */
    private CacheHandler cacheHandler;

    private NameProvider nameProvider;

    /**
     * 缓存key前缀
     */
    private String prefix; // 缓存key前缀

    /**
     * 缓存过期控制
     */
    private CacheExpire cacheExpire;

    /**
     * expire的数量
     */
    private int expireNum;

    /**
     * cacheBy相关信息
     */
    private List<CacheByItem> cacheByItems = new ArrayList<CacheByItem>();

    /**
     * 是否使用多key缓存
     */
    private boolean useMultipleKeys;

    private String interableProperty;

    public CacheDriverImpl(MethodDescriptor md, ASTRootNode rootNode, CacheHandler cacheHandler,
                           ParameterContext context, NameProvider nameProvider) {
        this.cacheHandler = cacheHandler;
        this.nameProvider = nameProvider;
        init(md, rootNode, context);
    }

    @Override
    public boolean isUseMultipleKeys() {
        return useMultipleKeys;
    }

    @Override
    public void setToCache(String key, Object value) {
        cacheHandler.set(key, value, cacheExpire.getExpireTime() * expireNum);
    }

    @Override
    public void deleteFromCache(String key) {
        cacheHandler.delete(key);
    }

    @Override
    public void deleteFromCache(Set<String> keys) {
        if (keys.size() > 0) {
            cacheHandler.delete(keys);
        }
    }

    @Override
    public Object getFromCache(String key) {
        Object value = cacheHandler.get(key);
        return value;
    }

    @Nullable
    @Override
    public Map<String, Object> getBulkFromCache(Set<String> keys) {
        if (keys.size() > 0) {
            Map<String, Object> values = cacheHandler.getBulk(keys);
            return values;
        }
        return null;
    }

    @Override
    public Class<?> getOnlyCacheByClassForMulti() {
        return getOnlyCacheByItem(cacheByItems).getClazz();
    }

    @Override
    public String getCacheKey(InvocationContext context) {
        return getCacheKey(getCacheByObjs(context));
    }

    @Override
    public String getCacheKey(Object... objs) {
        if (objs.length == 0) {
            throw new RuntimeException(); // TODO
        }
        StringBuilder key = new StringBuilder(prefix);
        for (Object obj : objs) {
            key.append("_").append(obj);
        }
        return key.toString();
    }

    @Override
    public Set<String> getCacheKeysForMulti(InvocationContext context) {
        Iterables iterables = new Iterables(getOnlyCacheByObjForMulti(context));
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
    public Object getOnlyCacheByObjForMulti(InvocationContext context) {
        CacheByItem item = getOnlyCacheByItem(cacheByItems);
        Object obj = context.getPropertyValue(item.getParameterName(), item.getInvoker());
        if (obj == null) {
            throw new NullPointerException("value of " + item.getFullName() + " can't be null");
        }
        return obj;
    }

    @Override
    public void setOnlyCacheByObjForMulti(InvocationContext context, Object obj) {
        CacheByItem item = getOnlyCacheByItem(cacheByItems);
        context.setPropertyValue(item.getParameterName(), item.getInvoker(), obj);
    }

    @Override
    public String getInterableProperty() {
        return interableProperty;
    }

    private void init(MethodDescriptor md, ASTRootNode rootNode, ParameterContext context) {
        for (ParameterDescriptor pd : md.getParameterDescriptors()) {
            CacheBy cacheByAnno = pd.getAnnotation(CacheBy.class);
            if (cacheByAnno != null) {
                String parameterName = nameProvider.getParameterName(pd.getPosition());
                String propertyPaths = cacheByAnno.value();
                for (String propertyPath : propertyPaths.split("\\.")) {
                    GetterInvoker invoker = context.getInvoker(parameterName, propertyPath);
                    Type cacheByType = invoker.getType();
                    TypeWrapper tw = new TypeWrapper(cacheByType);
                    cacheByItems.add(new CacheByItem(parameterName, propertyPath, tw.getMappedClass(), invoker));
                    useMultipleKeys = useMultipleKeys || tw.isIterable();
                }
            }
        }
        int cacheByNum = cacheByItems.size();
        if (useMultipleKeys && cacheByNum > 1) { // 当@CacheBy修饰in语句时，只能有1个@CacheBy
            throw new RuntimeException(); // TODO
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
                cacheExpire = Reflection.instantiate(cacheAnno.expire());
                expireNum = cacheAnno.num();
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
                    interableProperty = jip.getPropertyOfMapper();
                    break;
                }
            }
        }
    }

    private Object[] getCacheByObjs(InvocationContext context) {
        Object[] cacheByObjs = new Object[cacheByItems.size()];
        for (int i = 0; i < cacheByItems.size(); i++) {
            CacheByItem item = cacheByItems.get(i);
            Object obj = context.getPropertyValue(item.getParameterName(), item.getInvoker());
            if (obj == null) {
                throw new NullPointerException("value of " + item.getFullName() + " can't be null");
            }
            cacheByObjs[i] = obj;
        }
        return cacheByObjs;
    }

    private static CacheByItem getOnlyCacheByItem(List<CacheByItem> cacheByItems) {
        if (cacheByItems.size() != 1) {
            throw new RuntimeException(); // TODO
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

        private final Class<?> clazz;

        private final GetterInvoker invoker;

        public CacheByItem(String parameterName, String propertyPath, Class<?> clazz, GetterInvoker invoker) {
            this.parameterName = parameterName;
            this.propertyPath = propertyPath;
            this.clazz = clazz;
            this.invoker = invoker;
        }

        public String getParameterName() {
            return parameterName;
        }

        public String getPropertyPath() {
            return propertyPath;
        }

        public Class<?> getClazz() {
            return clazz;
        }

        private GetterInvoker getInvoker() {
            return invoker;
        }

        public String getFullName() {
            return Strings.getFullName(parameterName, propertyPath);
        }

    }

}
