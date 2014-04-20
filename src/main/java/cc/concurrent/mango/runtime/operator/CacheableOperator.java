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

import cc.concurrent.mango.Cache;
import cc.concurrent.mango.CacheBy;
import cc.concurrent.mango.CacheHandler;
import cc.concurrent.mango.CacheIgnored;
import cc.concurrent.mango.exception.IncorrectAnnotationException;
import cc.concurrent.mango.exception.IncorrectCacheByException;
import cc.concurrent.mango.runtime.CacheDescriptor;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.runtime.parser.ValuableParameter;
import cc.concurrent.mango.util.reflect.Reflection;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ash
 */
public abstract class CacheableOperator extends AbstractOperator implements Cacheable {

    private CacheHandler cacheHandler;
    private CacheDescriptor cacheDescriptor;

    protected CacheableOperator(Method method, SQLType sqlType) {
        super(method, sqlType);
        buildCacheDescriptor(method);
    }

    @Override
    public void setCacheHandler(@Nullable CacheHandler cacheHandler) {
        if (isUseCache() && cacheHandler == null) {
            throw new NullPointerException("if use cache, please provide an implementation of CacheHandler");
        }
        this.cacheHandler = cacheHandler;
    }

    protected void checkCacheBy(ASTRootNode rootNode) {
        if (isUseCache()) {
            String parameterName = cacheDescriptor.getParameterName();
            String propertyPath = cacheDescriptor.getPropertyPath();
            List<ValuableParameter> vps = rootNode.getValueValuableParameters();
            for (ValuableParameter vp : vps) {
                if (vp.getParameterName().equals(parameterName) &&
                        vp.getPropertyPath().equals(propertyPath)) {
                    return;
                }
            }
            String fullName = getFullName(parameterName, propertyPath);
            throw new IncorrectCacheByException("CacheBy " + fullName + " can't match any db parameter");
        }
    }

    protected boolean isUseCache() {
        return cacheDescriptor.isUseCache();
    }

    protected void setToCache(String key, Object value) {
        cacheHandler.set(key, value, cacheDescriptor.getExpires());
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

    public Map<String, Object> getBulkFromCache(Set<String> keys) {
        return cacheHandler.getBulk(keys);
    }

    protected Object getCacheKeyObj(RuntimeContext context) {
        return context.getPropertyValue(cacheDescriptor.getParameterName(), cacheDescriptor.getPropertyPath());
    }

    protected String getSingleKey(RuntimeContext context) {
        return getKey(getCacheKeyObj(context));
    }

    protected String getKey(Object keyObj) {
        return cacheDescriptor.getPrefix() + keyObj;
    }

    protected String getCacheParameterName() {
        return cacheDescriptor.getParameterName();
    }

    protected String getCachePropertyPath() {
        return cacheDescriptor.getPropertyPath();
    }

    private void buildCacheDescriptor(Method method) {
        Class<?> daoClass = method.getDeclaringClass();
        Cache cacheAnno = daoClass.getAnnotation(Cache.class);
        cacheDescriptor = new CacheDescriptor();
        if (cacheAnno != null) { // dao类使用cache
            CacheIgnored cacheIgnoredAnno = method.getAnnotation(CacheIgnored.class);
            if (cacheIgnoredAnno == null) { // method不禁用cache
                cacheDescriptor.setUseCache(true);
                cacheDescriptor.setPrefix(cacheAnno.prefix());
                cacheDescriptor.setExpire(Reflection.instantiate(cacheAnno.expire()));
                cacheDescriptor.setNum(cacheAnno.num());
                Annotation[][] pass = method.getParameterAnnotations();
                int num = 0;
                for (int i = 0; i < pass.length; i++) {
                    Annotation[] pas = pass[i];
                    for (Annotation pa : pas) {
                        if (CacheBy.class.equals(pa.annotationType())) {
                            cacheDescriptor.setParameterName(getParameterNameByIndex(i));
                            cacheDescriptor.setPropertyPath(((CacheBy) pa).value());
                            num++;
                        }
                    }
                }
                if (num != 1) {
                    throw new IncorrectAnnotationException("if use cache, each method " +
                            "expected one and only one cc.concurrent.mango.CacheBy annotation on parameter " +
                            "but found " + num);
                }
            }
        }
    }

    private String getFullName(String parameterName, String propertyPath) {
        return ":" + (!propertyPath.isEmpty() ? parameterName + "." + propertyPath : parameterName);
    }

}
