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
import org.jfaster.mango.datasource.DataSourceFactoryHolder;
import org.jfaster.mango.exception.IncorrectAnnotationException;
import org.jfaster.mango.exception.IncorrectCacheByException;
import org.jfaster.mango.exception.IncorrectDefinitionException;
import org.jfaster.mango.parser.node.ASTJDBCIterableParameter;
import org.jfaster.mango.parser.node.ASTJDBCParameter;
import org.jfaster.mango.parser.node.ASTRootNode;
import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.support.SQLType;
import org.jfaster.mango.util.Iterables;
import org.jfaster.mango.util.reflect.Reflection;
import org.jfaster.mango.util.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ash
 */
public class CacheableOperatorDriverImpl extends OperatorDriverImpl implements CacheableOperatorDriver {

    /**
     * 具体的缓存实现，通过{@link this#setCacheHandler(org.jfaster.mango.cache.CacheHandler)}初始化
     */
    private CacheHandler cacheHandler;

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

    private String interableProperty;

    public CacheableOperatorDriverImpl(DataSourceFactoryHolder dataSourceFactoryHolder, SQLType sqlType,
                                       OperatorType operatorType, Method method,
                                       ASTRootNode rootNode, CacheHandler cacheHandler) {
        super(dataSourceFactoryHolder, sqlType, operatorType, method, rootNode);
        this.cacheHandler = cacheHandler;
        init(method, rootNode);
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
    public String getCacheKey(RuntimeContext context) {
        return getCacheKey(getSuffixObj(context));
    }

    @Override
    public String getCacheKey(Object suffix) {
        return prefix + suffix;
    }

    @Override
    public Set<String> getCacheKeys(RuntimeContext context) {
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
    public Object getSuffixObj(RuntimeContext context) {
        Object obj = context.getPropertyValue(suffixParameterName, suffixPropertyPath);
        if (obj == null) {
            throw new NullPointerException("value of " + suffixFullName + " can't be null");
        }
        return obj;
    }

    @Override
    public void setSuffixObj(RuntimeContext context, Object obj) {
        context.setPropertyValue(suffixParameterName, suffixPropertyPath, obj);
    }

    @Override
    public String getInterableProperty() {
        return interableProperty;
    }

    private void init(Method method, ASTRootNode rootNode) {
        Annotation[][] pass = method.getParameterAnnotations();
        int cacheByNum = 0;
        for (int i = 0; i < pass.length; i++) {
            Annotation[] pas = pass[i];
            for (Annotation pa : pas) {
                if (CacheBy.class.equals(pa.annotationType())) {
                    suffixParameterName = getParameterNameByIndex(i);
                    suffixPropertyPath = ((CacheBy) pa).value();
                    cacheByNum++;
                }
            }
        }
        Class<?> daoClass = method.getDeclaringClass();
        CacheIgnored cacheIgnoredAnno = method.getAnnotation(CacheIgnored.class);
        Cache cacheAnno = daoClass.getAnnotation(Cache.class);
        if (cacheAnno != null) { // dao类使用cache
            if (cacheIgnoredAnno == null) { // method不禁用cache
                if (cacheByNum != 1) {
                    throw new IncorrectAnnotationException("if use cache, each method " +
                            "expected one and only one @CacheBy annotation on parameter " +
                            "but found " + cacheByNum);
                }
                prefix = cacheAnno.prefix();
                cacheExpire = Reflection.instantiate(cacheAnno.expire());
                expireNum = cacheAnno.num();
                checkCacheBy(rootNode);
                Type suffixType = getTypeContext().getPropertyType(suffixParameterName, suffixPropertyPath);
                TypeToken typeToken = new TypeToken(suffixType);
                useMultipleKeys = typeToken.isIterable();
                suffixClass = typeToken.getMappedClass();
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
            if (jip.getParameterName().equals(suffixParameterName)
                    && jip.getPropertyPath().equals(suffixPropertyPath)) {
                interableProperty = jip.getInterableProperty();
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
            if (jp.getParameterName().equals(suffixParameterName) &&
                    jp.getPropertyPath().equals(suffixPropertyPath)) {
                suffixFullName = jp.getFullName();
                return;
            }
        }
        List<ASTJDBCIterableParameter> jips = rootNode.getJDBCIterableParameters();
        for (ASTJDBCIterableParameter jip : jips) {
            if (jip.getParameterName().equals(suffixParameterName) &&
                    jip.getPropertyPath().equals(suffixPropertyPath)) {
                suffixFullName = jip.getFullName();
                return;
            }
        }
        String fullName = getFullName(suffixParameterName, suffixPropertyPath);
        throw new IncorrectCacheByException("CacheBy " + fullName + " can't match any db parameter");
    }

    private String getFullName(String parameterName, String propertyPath) {
        return ":" + (!propertyPath.isEmpty() ? parameterName + "." + propertyPath : parameterName);
    }

}
