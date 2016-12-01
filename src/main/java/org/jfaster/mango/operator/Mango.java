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
import org.jfaster.mango.datasource.SimpleDataSourceFactory;
import org.jfaster.mango.descriptor.MethodDescriptor;
import org.jfaster.mango.descriptor.Methods;
import org.jfaster.mango.descriptor.ParameterNameDiscover;
import org.jfaster.mango.descriptor.SerialNumberParameterNameDiscover;
import org.jfaster.mango.exception.InitializationException;
import org.jfaster.mango.interceptor.Interceptor;
import org.jfaster.mango.interceptor.InterceptorChain;
import org.jfaster.mango.jdbc.JdbcOperations;
import org.jfaster.mango.jdbc.JdbcTemplate;
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

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * mango框架DAO工厂
 *
 * @author ash
 */
public class Mango {

  private final static InternalLogger logger = InternalLoggerFactory.getInstance(Mango.class);

  /**
   * 数据源工厂
   */
  private DataSourceFactory dataSourceFactory;

  /**
   * 全局缓存处理器
   */
  private CacheHandler defaultCacheHandler;

  /**
   * 全局懒加载，默认为false
   */
  private boolean isDefaultLazyInit = false;

  /**
   * 拦截器链，默认为空
   */
  private InterceptorChain interceptorChain = new InterceptorChain();

  /**
   * jdbc操作
   */
  private JdbcOperations jdbcOperations = new JdbcTemplate();

  /**
   * 参数名发现器
   */
  private ParameterNameDiscover parameterNameDiscover = new SerialNumberParameterNameDiscover();

  /**
   * 统计收集器
   */
  private final StatCollector statCollector = new StatCollector();

  /**
   * mango全局配置信息
   */
  private ConfigHolder configHolder = new ConfigHolder();

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
    return newInstance().setDataSource(dataSource);
  }

  public static Mango newInstance(DataSourceFactory dataSourceFactory) {
    return newInstance().setDataSourceFactory(dataSourceFactory);
  }

  public static Mango newInstance(DataSourceFactory dataSourceFactory, CacheHandler cacheHandler) {
    return newInstance().setDataSourceFactory(dataSourceFactory).setDefaultCacheHandler(cacheHandler);
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
  public Mango addInterceptor(Interceptor interceptor) {
    if (interceptor == null) {
      throw new NullPointerException("interceptor can't be null");
    }
    if (interceptorChain == null) {
      interceptorChain = new InterceptorChain();
    }
    interceptorChain.addInterceptor(interceptor);
    return this;
  }

  /**
   * 创建代理DAO类
   */
  public <T> T create(Class<T> daoClass) {
    return create(daoClass, defaultCacheHandler, isDefaultLazyInit);
  }

  /**
   * 创建代理DAO类，使用特定的{@link CacheHandler}
   */
  public <T> T create(Class<T> daoClass, @Nullable CacheHandler cacheHandler) {
    return create(daoClass, cacheHandler, isDefaultLazyInit);
  }

  /**
   * 创建代理DAO类，自定义是否懒加载
   */
  public <T> T create(Class<T> daoClass, boolean isLazyInit) {
    return create(daoClass, defaultCacheHandler, isLazyInit);
  }

  /**
   * 创建代理DAO类，使用特定的{@link CacheHandler}，自定义是否懒加载
   */
  public <T> T create(Class<T> daoClass, @Nullable CacheHandler cacheHandler, boolean isLazyInit) {
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

    if (cacheHandler == null) {
      cacheHandler = defaultCacheHandler;
    }
    Cache cacheAnno = daoClass.getAnnotation(Cache.class);
    if (cacheAnno != null && cacheHandler == null) {
      throw new IllegalStateException("if @Cache annotation on dao interface, " +
          "cacheHandler can't be null");
    }

    if (dataSourceFactory == null) {
      throw new IllegalArgumentException("dataSourceFactory can't be null");
    }

    MangoInvocationHandler handler = new MangoInvocationHandler(this, cacheHandler);
    if (!isLazyInit) { // 不使用懒加载，则提前加载
      Method[] methods = daoClass.getMethods();
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
   * 根据数据源名字获得主库数据源
   */
  public DataSource getMasterDataSource(String database) {
    return dataSourceFactory.getMasterDataSource(database);
  }

  public Mango setDataSource(DataSource dataSource) {
    if (dataSource == null) {
      throw new NullPointerException("dataSource can't be null");
    }
    dataSourceFactory = new SimpleDataSourceFactory(dataSource);
    return this;
  }

  public DataSourceFactory getDataSourceFactory() {
    return dataSourceFactory;
  }

  public Mango setDataSourceFactory(DataSourceFactory dataSourceFactory) {
    if (dataSourceFactory == null) {
      throw new NullPointerException("dataSourceFactory can't be null");
    }
    this.dataSourceFactory = dataSourceFactory;
    return this;
  }

  public CacheHandler getDefaultCacheHandler() {
    return defaultCacheHandler;
  }

  public Mango setDefaultCacheHandler(CacheHandler defaultCacheHandler) {
    if (defaultCacheHandler == null) {
      throw new NullPointerException("defaultCacheHandler can't be null");
    }
    this.defaultCacheHandler = defaultCacheHandler;
    return this;
  }

  public boolean isDefaultLazyInit() {
    return isDefaultLazyInit;
  }

  public Mango setDefaultLazyInit(boolean isDefaultLazyInit) {
    this.isDefaultLazyInit = isDefaultLazyInit;
    return this;
  }

  public Mango setInterceptorChain(InterceptorChain interceptorChain) {
    if (interceptorChain == null) {
      throw new NullPointerException("interceptorChain can't be null");
    }
    this.interceptorChain = interceptorChain;
    return this;
  }

  public JdbcOperations getJdbcOperations() {
    return jdbcOperations;
  }

  public Mango setJdbcOperations(JdbcOperations jdbcOperations) {
    if (jdbcOperations == null) {
      throw new NullPointerException("jdbcOperations can't be null");
    }
    this.jdbcOperations = jdbcOperations;
    return this;
  }

  public ParameterNameDiscover getParameterNameDiscover() {
    return parameterNameDiscover;
  }

  public Mango setParameterNameDiscover(ParameterNameDiscover parameterNameDiscover) {
    if (parameterNameDiscover == null) {
      throw new NullPointerException("parameterNameDiscover can't be null");
    }
    this.parameterNameDiscover = parameterNameDiscover;
    return this;
  }

  public Config getConfig() {
    return configHolder.get();
  }

  public Mango setConfig(Config config) {
    configHolder.set(config);
    return this;
  }

  public Mango setStatMonitor(StatMonitor statMonitor) {
    statCollector.initStatMonitor(statMonitor);
    return this;
  }

  public void shutDownStatMonitor() {
    statCollector.shutDown();
  }

  private static class MangoInvocationHandler extends AbstractInvocationHandler implements InvocationHandler {

    private final StatCollector statCollector;
    private final OperatorFactory operatorFactory;
    private final ParameterNameDiscover parameterNameDiscover;

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
            MethodDescriptor md = Methods.getMethodDescriptor(method, parameterNameDiscover);
            Operator operator = operatorFactory.getOperator(md, metaStat);
            initStat.recordInit(System.nanoTime() - now);
            metaStat.setMethod(method);
            return operator;
          }
        });

    private MangoInvocationHandler(Mango mango, @Nullable CacheHandler cacheHandler) {
      statCollector = mango.statCollector;
      operatorFactory = new OperatorFactory(mango.dataSourceFactory, cacheHandler,
          mango.interceptorChain, mango.jdbcOperations, mango.configHolder);
      parameterNameDiscover = mango.parameterNameDiscover;
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
      if (logger.isDebugEnabled()) {
        logger.debug("Invoking {}", ToStringHelper.toString(method));
      }
      Operator operator = getOperator(method);
      OneExecuteStat stat = OneExecuteStat.create();
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
