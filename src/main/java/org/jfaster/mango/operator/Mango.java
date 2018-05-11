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
import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.datasource.DataSourceFactory;
import org.jfaster.mango.datasource.DataSourceFactoryGroup;
import org.jfaster.mango.datasource.SimpleDataSourceFactory;
import org.jfaster.mango.descriptor.MethodDescriptor;
import org.jfaster.mango.descriptor.Methods;
import org.jfaster.mango.exception.InitializationException;
import org.jfaster.mango.interceptor.Interceptor;
import org.jfaster.mango.interceptor.InterceptorChain;
import org.jfaster.mango.operator.cache.CacheHandler;
import org.jfaster.mango.stat.*;
import org.jfaster.mango.util.ToStringHelper;
import org.jfaster.mango.util.local.CacheLoader;
import org.jfaster.mango.util.local.DoubleCheckCache;
import org.jfaster.mango.util.local.LoadingCache;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;
import org.jfaster.mango.util.reflect.AbstractInvocationHandler;
import org.jfaster.mango.util.reflect.Reflection;

import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * mango框架DAO工厂
 *
 * @author ash
 */
public class Mango extends Config {

  private final static InternalLogger logger = InternalLoggerFactory.getInstance(Mango.class);

  /**
   * 数据源工厂组
   */
  private DataSourceFactoryGroup dataSourceFactoryGroup;

  /**
   * 缓存处理器
   */
  private CacheHandler cacheHandler;

  /**
   * 是否懒加载
   */
  private boolean isLazyInit = false;

  /**
   * 拦截器链，默认为空
   */
  private InterceptorChain interceptorChain = new InterceptorChain();

  /**
   * 统计收集器
   */
  private final StatCollector statCollector = new StatCollector();

  /**
   * mango实例
   */
  private final static CopyOnWriteArrayList<Mango> instances = new CopyOnWriteArrayList<Mango>();

  private Mango() {
  }

  public synchronized static Mango newInstance() {
    if (instances.size() == 1) {
      if (logger.isWarnEnabled()) {
        logger.warn("Find out more mango instances, it is recommended to use only one");
      }
    }
    Mango mango = new Mango();
    instances.add(mango);
    return mango;
  }

  public static Mango newInstance(DataSource dataSource) {
    Mango mango = newInstance();
    mango.setDataSource(dataSource);
    return mango;
  }

  public static Mango newInstance(DataSourceFactory dataSourceFactory) {
    Mango mango = newInstance();
    mango.setDataSourceFactory(dataSourceFactory);
    return mango;
  }

  public static Mango newInstance(DataSourceFactory... dataSourceFactories) {
    return newInstance(Arrays.asList(dataSourceFactories));
  }

  public static Mango newInstance(List<DataSourceFactory> dataSourceFactories) {
    Mango mango = newInstance();
    mango.setDataSourceFactories(dataSourceFactories);
    return mango;
  }

  public static Mango newInstance(DataSourceFactory dataSourceFactory, CacheHandler cacheHandler) {
    Mango mango = newInstance();
    mango.setDataSourceFactory(dataSourceFactory);
    mango.setCacheHandler(cacheHandler);
    return mango;
  }

  public static Mango newInstance(List<DataSourceFactory> dataSourceFactories, CacheHandler cacheHandler) {
    Mango mango = newInstance();
    mango.setDataSourceFactories(dataSourceFactories);
    mango.setCacheHandler(cacheHandler);
    return mango;
  }

  /**
   * 获得mango实例
   */
  public static List<Mango> getInstances() {
    List<Mango> mangos = new ArrayList<Mango>();
    for (Mango instance : instances) {
      mangos.add(instance);
    }
    return Collections.unmodifiableList(mangos);
  }

  /**
   * 添加拦截器
   */
  public void addInterceptor(Interceptor interceptor) {
    if (interceptor == null) {
      throw new NullPointerException("interceptor can't be null");
    }
    if (interceptorChain == null) {
      interceptorChain = new InterceptorChain();
    }
    interceptorChain.addInterceptor(interceptor);
  }

  /**
   * 创建代理DAO类
   */
  public <T> T create(Class<T> daoClass) {
    if (daoClass == null) {
      throw new NullPointerException("dao interface can't be null");
    }

    if (!daoClass.isInterface()) {
      throw new IllegalArgumentException("expected an interface to proxy, but " + daoClass);
    }

    DB dbAnno = daoClass.getAnnotation(DB.class);
    if (dbAnno == null) {
      throw new IllegalStateException("dao interface expected one @DB " +
          "annotation but not found");
    }

    Cache cacheAnno = daoClass.getAnnotation(Cache.class);
    if (cacheAnno != null && cacheHandler == null) {
      throw new IllegalStateException("if @Cache annotation on dao interface, " +
          "cacheHandler can't be null");
    }

    if (dataSourceFactoryGroup == null) {
      throw new IllegalArgumentException("please set dataSource or dataSourceFactory or dataSourceFactories");
    }

    MangoInvocationHandler handler = new MangoInvocationHandler(
        daoClass, dataSourceFactoryGroup, cacheHandler, interceptorChain, statCollector, this);
    if (!isLazyInit) { // 不使用懒加载，则提前加载
      List<Method> methods = Methods.listMethods(daoClass);
      for (Method method : methods) {
        try {
          handler.getOperator(method);
        } catch (Throwable e) {
          throw new InitializationException("initialize " + ToStringHelper.toString(method) + " error", e);
        }
      }
    }
    return Reflection.newProxy(daoClass, handler);
  }

  /**
   * 返回状态信息
   */
  public StatInfo getStatInfo() {
    return statCollector.getStatInfo();
  }

  /**
   * 根据数据源工厂名字获得主库数据源
   */
  public DataSource getMasterDataSource(String name) {
    return dataSourceFactoryGroup.getMasterDataSource(name);
  }

  public void setDataSource(DataSource dataSource) {
    if (dataSource == null) {
      throw new NullPointerException("dataSource can't be null");
    }
    setDataSourceFactory(new SimpleDataSourceFactory(dataSource));
  }

  public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
    if (dataSourceFactory == null) {
      throw new NullPointerException("dataSourceFactory can't be null");
    }
    setDataSourceFactories(Arrays.asList(dataSourceFactory));
  }

  public void addDataSourceFactory(DataSourceFactory dataSourceFactory) {
    if (dataSourceFactory == null) {
      throw new NullPointerException("dataSourceFactory can't be null");
    }
    if (dataSourceFactoryGroup == null) {
      dataSourceFactoryGroup = new DataSourceFactoryGroup();
    }
    dataSourceFactoryGroup.addDataSourceFactory(dataSourceFactory);
  }

  public void setDataSourceFactories(List<DataSourceFactory> dataSourceFactories) {
    if (dataSourceFactories == null || dataSourceFactories.isEmpty()) {
      throw new IllegalArgumentException("dataSourceFactories can't be null or empty");
    }
    dataSourceFactoryGroup = new DataSourceFactoryGroup(dataSourceFactories);
  }

  public CacheHandler getCacheHandler() {
    return cacheHandler;
  }

  public void setCacheHandler(CacheHandler cacheHandler) {
    if (cacheHandler == null) {
      throw new NullPointerException("cacheHandler can't be null");
    }
    this.cacheHandler = cacheHandler;
  }

  public boolean isLazyInit() {
    return isLazyInit;
  }

  public void setLazyInit(boolean isLazyInit) {
    this.isLazyInit = isLazyInit;
  }

  public void setInterceptorChain(InterceptorChain interceptorChain) {
    if (interceptorChain == null) {
      throw new NullPointerException("interceptorChain can't be null");
    }
    this.interceptorChain = interceptorChain;
  }

  public void setStatMonitor(StatMonitor statMonitor) {
    statCollector.initStatMonitor(statMonitor);
  }

  public void shutDownStatMonitor() {
    statCollector.shutDown();
  }

  private static class MangoInvocationHandler extends AbstractInvocationHandler implements InvocationHandler {

    private final Class<?> daoClass;
    private final StatCollector statCollector;
    private final OperatorFactory operatorFactory;
    private final boolean isUseActualParamName;

    private final LoadingCache<Method, Operator> cache = new DoubleCheckCache<Method, Operator>(
        new CacheLoader<Method, Operator>() {
          public Operator load(Method method) {
            if (logger.isInfoEnabled()) {
              logger.info("Initializing operator for {}", ToStringHelper.toString(method));
            }
            CombinedStat combinedStat = statCollector.getCombinedStat(method);
            MetaStat metaStat = combinedStat.getMetaStat();
            InitStat initStat = combinedStat.getInitStat();
            long now = System.nanoTime();
            MethodDescriptor md = Methods.getMethodDescriptor(daoClass, method, isUseActualParamName);
            Operator operator = operatorFactory.getOperator(md, metaStat);
            initStat.recordInit(System.nanoTime() - now);
            metaStat.setDaoClass(daoClass);
            metaStat.setMethod(method);
            metaStat.setSql(md.getSQL());
            return operator;
          }
        });

    private MangoInvocationHandler(
        Class<?> daoClass,
        DataSourceFactoryGroup dataSourceFactoryGroup,
        CacheHandler cacheHandler,
        InterceptorChain interceptorChain,
        StatCollector statCollector,
        Config config) {
      this.daoClass = daoClass;
      this.statCollector = statCollector;
      this.isUseActualParamName = config.isUseActualParamName();
      operatorFactory = new OperatorFactory(dataSourceFactoryGroup, cacheHandler, interceptorChain, config);
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
      if (logger.isDebugEnabled()) {
        logger.debug("Invoking {}", ToStringHelper.toString(method));
      }
      Operator operator = getOperator(method);
      InvocationStat stat = InvocationStat.create();
      try {
        Object r = operator.execute(args, stat);
        return r;
      } finally {
        statCollector.getCombinedStat(method).getExecuteStat().accumulate(stat);
      }
    }

    Operator getOperator(Method method) {
      return cache.get(method);
    }

  }

}
