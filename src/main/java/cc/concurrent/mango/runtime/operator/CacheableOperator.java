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

import cc.concurrent.mango.*;
import cc.concurrent.mango.exception.IncorrectAnnotationException;
import cc.concurrent.mango.exception.IncorrectCacheByException;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.runtime.parser.ValuableParameter;
import cc.concurrent.mango.util.TypeToken;
import cc.concurrent.mango.util.reflect.Reflection;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
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
     * 具体的缓存实现，通过{@link this#setCacheHandler(cc.concurrent.mango.CacheHandler)}初始化
     */
    private CacheHandler cacheHandler;

    /**
     * 是否使用缓存
     */
    private boolean useCache;

    /**
     * 缓存key前缀
     */
    private String keyPrefix; // 缓存key前缀

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
    private String keySuffixParameterName;

    /**
     * 缓存后缀属性路径
     */
    private String keySuffixPropertyPath;

    /**
     * 是否使用多key缓存
     */
    private boolean useMultipleKeys;

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

    protected void setToCache(String key, Object value) {
        cacheHandler.set(key, value, cacheExpire.getExpireTime() * expireNum);
    }

    protected void deleteFromCache(String key) {
        cacheHandler.delete(key);
    }

    protected void deleteFromCache(Set<String> keys) {
        cacheHandler.delete(keys);
    }

    protected Object getFromCache(String key) {
        return cacheHandler.get(key);
    }

    protected Map<String, Object> getBulkFromCache(Set<String> keys) {
        return cacheHandler.getBulk(keys);
    }

    protected Object getKeySuffixObj(RuntimeContext context) {
        return context.getPropertyValue(keySuffixParameterName, keySuffixPropertyPath);
    }

    protected String getKey(RuntimeContext context) {
        return getKey(getKeySuffixObj(context));
    }

    protected String getKey(Object keySuffix) {
        return keyPrefix + keySuffix;
    }

    protected String getKeySuffixParameterName() {
        return keySuffixParameterName;
    }

    protected String getKeySuffixPropertyPath() {
        return keySuffixPropertyPath;
    }



    private void init() {
        Class<?> daoClass = method.getDeclaringClass();
        Cache cacheAnno = daoClass.getAnnotation(Cache.class);
        if (cacheAnno != null) { // dao类使用cache
            CacheIgnored cacheIgnoredAnno = method.getAnnotation(CacheIgnored.class);
            if (cacheIgnoredAnno == null) { // method不禁用cache
                useCache = true;
                keyPrefix = cacheAnno.prefix();
                cacheExpire = Reflection.instantiate(cacheAnno.expire());
                expireNum = cacheAnno.num();

                Annotation[][] pass = method.getParameterAnnotations();
                int num = 0;
                for (int i = 0; i < pass.length; i++) {
                    Annotation[] pas = pass[i];
                    for (Annotation pa : pas) {
                        if (CacheBy.class.equals(pa.annotationType())) {
                            keySuffixParameterName = getParameterNameByIndex(i);
                            keySuffixPropertyPath = ((CacheBy) pa).value();
                            num++;
                        }
                    }
                }
                if (num != 1) {
                    throw new IncorrectAnnotationException("if use cache, each method " +
                            "expected one and only one cc.concurrent.mango.CacheBy annotation on parameter " +
                            "but found " + num);
                }

                checkCacheBy();

                Type suffixType = getTypeContext().getPropertyType(keySuffixParameterName, keySuffixPropertyPath);
                TypeToken typeToken = new TypeToken(suffixType);
                useMultipleKeys = typeToken.isIterable();
            }
        }
    }

    /**
     * 检测{@link CacheBy}定位到的参数db中是否有用到，如果db中没有用到，则抛出{@link IncorrectCacheByException}
     */
    private void checkCacheBy() {
        List<ValuableParameter> vps = rootNode.getValueValuableParameters();
        for (ValuableParameter vp : vps) {
            if (vp.getParameterName().equals(keySuffixParameterName) &&
                    vp.getPropertyPath().equals(keySuffixPropertyPath)) {
                return;
            }
        }
        String fullName = getFullName(keySuffixParameterName, keySuffixPropertyPath);
        throw new IncorrectCacheByException("CacheBy " + fullName + " can't match any db parameter");
    }

    private String getFullName(String parameterName, String propertyPath) {
        return ":" + (!propertyPath.isEmpty() ? parameterName + "." + propertyPath : parameterName);
    }

    protected void cacheInitPostProcessor() {
    }

}
