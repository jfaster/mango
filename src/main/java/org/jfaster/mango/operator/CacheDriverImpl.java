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

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
     * 缓存后缀参数名
     */
    private String suffixParameterName;

    /**
     * 缓存后缀属性路径
     */
    private String suffixParameterProperty;

    private GetterInvoker invoker;

    /**
     * 缓存前缀全名
     */
    private String suffixFullName;

    /**
     * 是否使用多key缓存
     */
    private boolean useMultipleKeys;

    /**
     * 缓存后缀Class
     */
    private Class<?> suffixClass;

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

    @Override
    public Map<String, Object> getBulkFromCache(Set<String> keys) {
        if (keys.size() > 0) {
            Map<String, Object> values = cacheHandler.getBulk(keys);
            return values;
        }
        return null;
    }

    @Override
    public Class<?> getSuffixClass() {
        return suffixClass;
    }

    @Override
    public String getCacheKey(InvocationContext context) {
        return getCacheKey(getSuffixObj(context));
    }

    @Override
    public String getCacheKey(Object suffix) {
        return prefix + suffix;
    }

    @Override
    public Set<String> getCacheKeys(InvocationContext context) {
        Iterables iterables = new Iterables(getSuffixObj(context));
        if (iterables.isEmpty()) {
            throw new IllegalArgumentException("value of " + suffixFullName + " can't be empty");
        }
        Set<String> keys = new HashSet<String>(iterables.size() * 2);
        for (Object suffix : iterables) {
            String key = getCacheKey(suffix);
            keys.add(key);
        }
        return keys;
    }

    @Override
    public Object getSuffixObj(InvocationContext context) {
        Object obj = context.getPropertyValue(suffixParameterName, invoker);
        if (obj == null) {
            throw new NullPointerException("value of " + suffixFullName + " can't be null");
        }
        return obj;
    }

    @Override
    public void setSuffixObj(InvocationContext context, Object obj) {
        context.setPropertyValue(suffixParameterName, invoker, obj);
    }

    @Override
    public String getInterableProperty() {
        return interableProperty;
    }

    private void init(MethodDescriptor md, ASTRootNode rootNode, ParameterContext context) {
        int cacheByNum = 0;
        for (ParameterDescriptor pd : md.getParameterDescriptors()) {
            CacheBy cacheByAnno = pd.getAnnotation(CacheBy.class);
            if (cacheByAnno != null) {
                suffixParameterName = nameProvider.getParameterName(pd.getPosition());
                suffixParameterProperty = cacheByAnno.value();
                cacheByNum++;
            }
        }

        CacheIgnored cacheIgnoredAnno = md.getAnnotation(CacheIgnored.class);
        Cache cacheAnno = md.getAnnotation(Cache.class);
        if (cacheAnno != null) { // dao类使用cache
            if (cacheIgnoredAnno == null) { // method不禁用cache
                if (cacheByNum != 1) {
                    throw new IllegalStateException("if use cache, each method " +
                            "expected one and only one @CacheBy annotation on parameter " +
                            "but found " + cacheByNum);
                }
                prefix = cacheAnno.prefix();
                cacheExpire = Reflection.instantiate(cacheAnno.expire());
                expireNum = cacheAnno.num();
                checkCacheBy(rootNode);
                invoker = context.getInvoker(suffixParameterName, suffixParameterProperty);
                Type suffixType = invoker.getType();
                TypeWrapper tw = new TypeWrapper(suffixType);
                useMultipleKeys = tw.isIterable();
                suffixClass = tw.getMappedClass();
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

        for (ASTJDBCIterableParameter jip : rootNode.getJDBCIterableParameters()) {
            if (jip.getName().equals(suffixParameterName)
                    && jip.getProperty().equals(suffixParameterProperty)) {
                interableProperty = jip.getPropertyOfMapper();
                break;
            }
        }
    }

    /**
     * 检测{@link CacheBy}定位到的参数db中是否有用到，如果db中没有用到，则抛出{@link org.jfaster.mango.exception.IncorrectCacheByException}
     */
    private void checkCacheBy(ASTRootNode rootNode) {
        List<ASTJDBCParameter> jps = rootNode.getJDBCParameters();
        for (ASTJDBCParameter jp : jps) {
            if (jp.getName().equals(suffixParameterName) &&
                    jp.getProperty().equals(suffixParameterProperty)) {
                suffixFullName = jp.getFullName();
                return;
            }
        }
        List<ASTJDBCIterableParameter> jips = rootNode.getJDBCIterableParameters();
        for (ASTJDBCIterableParameter jip : jips) {
            if (jip.getName().equals(suffixParameterName) &&
                    jip.getProperty().equals(suffixParameterProperty)) {
                suffixFullName = jip.getFullName();
                return;
            }
        }
        String fullName = Strings.getFullName(suffixParameterName, suffixParameterProperty);
        throw new IncorrectCacheByException("CacheBy " + fullName + " can't match any db parameter");
    }

}
