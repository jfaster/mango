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
import org.jfaster.mango.binding.BindingParameter;
import org.jfaster.mango.binding.BindingParameterInvoker;
import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.binding.ParameterContext;
import org.jfaster.mango.descriptor.MethodDescriptor;
import org.jfaster.mango.descriptor.ParameterDescriptor;
import org.jfaster.mango.exception.DescriptionException;
import org.jfaster.mango.parser.ASTJDBCIterableParameter;
import org.jfaster.mango.parser.ASTJDBCParameter;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.stat.InvocationStat;
import org.jfaster.mango.util.Iterables;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.reflect.Reflection;
import org.jfaster.mango.util.reflect.TypeWrapper;

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

  private Class<?> daoClass;

  private Type returnType;

  private Type elementType;

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

  public CacheDriver(MethodDescriptor md, ASTRootNode rootNode, CacheHandler cacheHandler, ParameterContext context) {
    this.cacheHandler = cacheHandler;
    this.daoClass = md.getDaoClass();
    this.returnType = md.getReturnType();
    this.elementType = md.getReturnDescriptor().getMappedType();
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
  public void setToCache(String key, Object value, InvocationStat stat) {
    boolean success = false;
    long now = System.nanoTime();
    try {
      cacheHandler.set(key, value, getExptimeSeconds(), daoClass);
      success = true;
    } finally {
      long cost = System.nanoTime() - now;
      if (success) {
        stat.recordCacheSetSuccess(cost);
      } else {
        stat.recordCacheSetException(cost);
      }
    }
  }

  @Override
  public void addToCache(String key, Object value, InvocationStat stat) {
    boolean success = false;
    long now = System.nanoTime();
    try {
      cacheHandler.add(key, value, getExptimeSeconds(), daoClass);
      success = true;
    } finally {
      long cost = System.nanoTime() - now;
      if (success) {
        stat.recordCacheAddSuccess(cost);
      } else {
        stat.recordCacheAddException(cost);
      }
    }
  }

  @Override
  public void deleteFromCache(String key, InvocationStat stat) {
    boolean success = false;
    long now = System.nanoTime();
    try {
      cacheHandler.delete(key, daoClass);
      success = true;
    } finally {
      long cost = System.nanoTime() - now;
      if (success) {
        stat.recordCacheDeleteSuccess(cost);
      } else {
        stat.recordCacheDeleteException(cost);
      }
    }
  }

  @Override
  public void batchDeleteFromCache(Set<String> keys, InvocationStat stat) {
    if (keys.size() > 0) {
      boolean success = false;
      long now = System.nanoTime();
      try {
        cacheHandler.batchDelete(keys, daoClass);
        success = true;
      } finally {
        long cost = System.nanoTime() - now;
        if (success) {
          stat.recordCacheBatchDeleteSuccess(cost);
        } else {
          stat.recordCacheBatchDeleteException(cost);
        }
      }
    }
  }

  @Nullable
  @Override
  public Object getFromCache(String key, InvocationStat stat) {
    boolean success = false;
    long now = System.nanoTime();
    try {
      Object value = cacheHandler.get(key, returnType, daoClass);
      success = true;
      return value;
    } finally {
      long cost = System.nanoTime() - now;
      if (success) {
        stat.recordCacheGetSuccess(cost);
      } else {
        stat.recordCacheGetException(cost);
      }
    }
  }

  @Nullable
  @Override
  public Map<String, Object> getBulkFromCache(Set<String> keys, InvocationStat stat) {
    if (keys.size() > 0) {
      boolean success = false;
      long now = System.nanoTime();
      try {
        Map<String, Object> values = cacheHandler.getBulk(keys, elementType, daoClass);
        success = true;
        return values;
      } finally {
        long cost = System.nanoTime() - now;
        if (success) {
          stat.recordCacheGetBulkSuccess(cost);
        } else {
          stat.recordCacheGetBulkException(cost);
        }
      }
    }
    return null;
  }

  @Override
  public String getCacheKey(InvocationContext context) {
    StringBuilder key = new StringBuilder(prefix);
    for (CacheByItem item : cacheByItems) {
      Object obj = context.getBindingValue(item.getbindingParameterInvoker());
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
    Set<String> keys = new HashSet<String>();
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
    Object obj = context.getBindingValue(item.getbindingParameterInvoker());
    if (obj == null) {
      throw new NullPointerException("value of " + item.getFullName() + " can't be null");
    }
    return obj;
  }

  @Override
  public void setOnlyCacheByObj(InvocationContext context, Object obj) {
    CacheByItem item = getOnlyCacheByItem(cacheByItems);
    context.setBindingValue(item.getbindingParameterInvoker(), obj);
  }

  @Override
  public String getPropertyOfMapper() {
    return propertyOfMapper;
  }

  @Override
  public int getExptimeSeconds() {
    return cacheExpire.getExpireTime() * expireNum;
  }

  private void init(MethodDescriptor md, ASTRootNode rootNode, ParameterContext context) {
    for (ParameterDescriptor pd : md.getParameterDescriptors()) {
      CacheBy cacheByAnno = pd.getAnnotation(CacheBy.class);
      if (cacheByAnno != null) {
        String parameterName = context.getParameterNameByPosition(pd.getPosition());
        String propertyPaths = cacheByAnno.value();
        for (String propertyPath : propertyPaths.split(",")) {
          propertyPath = propertyPath.trim();
          BindingParameterInvoker invoker = context.getBindingParameterInvoker(BindingParameter.create(parameterName, propertyPath, null));
          Type cacheByType = invoker.getTargetType();
          TypeWrapper tw = new TypeWrapper(cacheByType);
          cacheByItems.add(new CacheByItem(parameterName, propertyPath, tw.getMappedClass(), invoker));
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
          throw new DescriptionException("if @CacheIgnored is on method, " +
              "@CacheBy can not on method's parameter");
        }
      }
    } else {
      if (cacheByNum > 0) {
        throw new DescriptionException("if @Cache is not defined, " +
            "@CacheBy can not on method's parameter");
      }
      if (cacheIgnoredAnno != null) {
        throw new DescriptionException("if @Cache is not defined, " +
            "@CacheIgnored can not on method");
      }
    }

    if (useMultipleKeys) {
      CacheByItem cacheByItem = getOnlyCacheByItem(cacheByItems);
      for (ASTJDBCIterableParameter jip : rootNode.getJDBCIterableParameters()) {
        if (jip.getBindingParameter().getParameterName().equals(cacheByItem.getParameterName())
            && jip.getBindingParameter().getPropertyPath().equals(cacheByItem.getPropertyPath())) {
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
   * 则抛出{@link IncorrectCacheByException}
   */
  private static void checkCacheBy(ASTRootNode rootNode, List<CacheByItem> cacheByItems) {
    List<ASTJDBCParameter> jps = rootNode.getJDBCParameters();
    for (CacheByItem cacheByItem : cacheByItems) {
      String parameterName = cacheByItem.getParameterName();
      String propertyPath = cacheByItem.getPropertyPath();
      boolean pass = false;
      for (ASTJDBCParameter jp : jps) {
        if (jp.getBindingParameter().getParameterName().equals(parameterName) &&
            jp.getBindingParameter().getPropertyPath().equals(propertyPath)) {
          pass = true;
          break;
        }
      }
      List<ASTJDBCIterableParameter> jips = rootNode.getJDBCIterableParameters();
      for (ASTJDBCIterableParameter jip : jips) {
        if (jip.getBindingParameter().getParameterName().equals(parameterName) &&
            jip.getBindingParameter().getPropertyPath().equals(propertyPath)) {
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

    private final BindingParameterInvoker bindingParameterInvoker;

    public CacheByItem(
        String parameterName,
        String propertyPath,
        Class<?> actualClass,
        BindingParameterInvoker bindingParameterInvoker) {
      this.parameterName = parameterName;
      this.propertyPath = propertyPath;
      this.actualClass = actualClass;
      this.bindingParameterInvoker = bindingParameterInvoker;
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

    private BindingParameterInvoker getbindingParameterInvoker() {
      return bindingParameterInvoker;
    }

    public String getFullName() {
      return Strings.getFullName(parameterName, propertyPath);
    }

  }

}
