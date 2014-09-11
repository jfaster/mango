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
import org.jfaster.mango.exception.IncorrectAnnotationException;
import org.jfaster.mango.exception.IncorrectCacheByException;
import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.parser.ValuableParameter;
import org.jfaster.mango.util.Iterables;
import org.jfaster.mango.support.SQLType;
import org.jfaster.mango.util.reflect.TypeToken;
import org.jfaster.mango.util.reflect.Reflection;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 可被缓存的操作
 *
 * @author ash
 */
public abstract class CacheableOperator extends AbstractOperator implements Cacheable {

    /**
     * 具体的缓存实现，通过{@link this#setCacheHandler(org.jfaster.mango.cache.CacheHandler)}初始化
     */
    private CacheHandler cacheHandler;

    /**
     * 是否使用缓存
     */
    private boolean useCache;

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
    private String suffixPropertyPath;

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

    protected CacheableOperator(ASTRootNode rootNode, Method method, SQLType sqlType) {
        super(rootNode, method, sqlType);
        init();
        cacheInitPostProcessor();
    }

    @Override
    public void setCacheHandler(@Nullable CacheHandler cacheHandler) {
        if (isUseCache() && cacheHandler == null) {
            throw new NullPointerException("if use cache, please provide an implementation of CacheHandler");
        }
        this.cacheHandler = cacheHandler;
    }

    protected boolean isUseCache() {
        return useCache;
    }

    protected boolean isUseMultipleKeys() {
        return useMultipleKeys;
    }

    protected void setToCache(String key, Object value) {
        cacheHandler.set(key, value, cacheExpire.getExpireTime() * expireNum);
    }

    protected void deleteFromCache(String key) {
        cacheHandler.delete(key);
        statsCounter.recordEviction(1);
    }

    protected void deleteFromCache(Set<String> keys) {
        if (keys.size() > 0) {
            cacheHandler.delete(keys);
            statsCounter.recordEviction(keys.size());
        }
    }

    protected Object getFromCache(String key) {
        Object value = cacheHandler.get(key);
        if (value != null) {
            statsCounter.recordHits(1);
        } else {
            statsCounter.recordMisses(1);
        }
        return value;
    }

    protected Map<String, Object> getBulkFromCache(Set<String> keys) {
        if (keys.size() > 0) {
            Map<String, Object> values = cacheHandler.getBulk(keys);
            int hitCount = values.size();
            int missCount = keys.size() - hitCount;
            if (hitCount > 0) {
               statsCounter.recordHits(hitCount);
            }
            if (missCount > 0) {
                statsCounter.recordMisses(missCount);
            }
            return values;
        }
        return null;
    }

    protected Class<?> getSuffixClass() {
        return suffixClass;
    }

    protected String getCacheKey(RuntimeContext context) {
        return getCacheKey(getSuffixObj(context));
    }

    protected String getCacheKey(Object suffix) {
        return prefix + suffix;
    }

    protected Set<String> getCacheKeys(RuntimeContext context) {
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

    protected Object getSuffixObj(RuntimeContext context) {
        Object obj = context.getPropertyValue(suffixParameterName, suffixPropertyPath);
        if (obj == null) {
            throw new NullPointerException("value of " + suffixFullName + " can't be null");
        }
        return obj;
    }

    protected void setSuffixObj(RuntimeContext context, Object obj) {
        context.setPropertyValue(suffixParameterName, suffixPropertyPath, obj);
    }

    private void init() {
        Class<?> daoClass = method.getDeclaringClass();
        Cache cacheAnno = daoClass.getAnnotation(Cache.class);
        if (cacheAnno != null) { // dao类使用cache
            CacheIgnored cacheIgnoredAnno = method.getAnnotation(CacheIgnored.class);
            if (cacheIgnoredAnno == null) { // method不禁用cache
                useCache = true;
                prefix = cacheAnno.prefix();
                cacheExpire = Reflection.instantiate(cacheAnno.expire());
                expireNum = cacheAnno.num();

                Annotation[][] pass = method.getParameterAnnotations();
                int num = 0;
                for (int i = 0; i < pass.length; i++) {
                    Annotation[] pas = pass[i];
                    for (Annotation pa : pas) {
                        if (CacheBy.class.equals(pa.annotationType())) {
                            suffixParameterName = getParameterNameByIndex(i);
                            suffixPropertyPath = ((CacheBy) pa).value();
                            num++;
                        }
                    }
                }
                if (num != 1) {
                    throw new IncorrectAnnotationException("if use cache, each method " +
                            "expected one and only one @CacheBy annotation on parameter " +
                            "but found " + num);
                }

                checkCacheBy();

                Type suffixType = getTypeContext().getPropertyType(suffixParameterName, suffixPropertyPath);
                TypeToken typeToken = new TypeToken(suffixType);
                useMultipleKeys = typeToken.isIterable();
                suffixClass = typeToken.getMappedClass();
            }
        }
    }

    /**
     * 检测{@link CacheBy}定位到的参数db中是否有用到，如果db中没有用到，则抛出{@link IncorrectCacheByException}
     */
    private void checkCacheBy() {
        List<ValuableParameter> vps = rootNode.getValuableParameters();
        for (ValuableParameter vp : vps) {
            if (vp.getParameterName().equals(suffixParameterName) &&
                    vp.getPropertyPath().equals(suffixPropertyPath)) {
                suffixFullName =  vp.getFullName();
                return;
            }
        }
        String fullName = getFullName(suffixParameterName, suffixPropertyPath);
        throw new IncorrectCacheByException("CacheBy " + fullName + " can't match any db parameter");
    }

    private String getFullName(String parameterName, String propertyPath) {
        return ":" + (!propertyPath.isEmpty() ? parameterName + "." + propertyPath : parameterName);
    }

    protected void cacheInitPostProcessor() {
    }

}
