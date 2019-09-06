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

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.datasource.DataSourceFactory;
import org.jfaster.mango.datasource.DataSourceFactoryGroup;
import org.jfaster.mango.datasource.SimpleDataSourceFactory;
import org.jfaster.mango.descriptor.MethodDescriptor;
import org.jfaster.mango.descriptor.Methods;
import org.jfaster.mango.page.MySQLPageHandler;
import org.jfaster.mango.page.PageHandler;
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
   * 默认使用MySQL分页处理器
   */
  private PageHandler pageHandler = new MySQLPageHandler();

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

    if (dataSourceFactoryGroup == null) {
      throw new IllegalArgumentException("please set dataSource or dataSourceFactory or dataSourceFactories");
    }

    MangoInvocationHandler handler = new MangoInvocationHandler(
        daoClass, dataSourceFactoryGroup, pageHandler, this);
    return Reflection.newProxy(daoClass, handler);
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

  public void setPageHandler(PageHandler pageHandler) {
    if (pageHandler == null) {
      throw new NullPointerException("pageHandler can't be null");
    }
    this.pageHandler = pageHandler;
  }

  private static class MangoInvocationHandler extends AbstractInvocationHandler implements InvocationHandler {

    private final Class<?> daoClass;
    private final OperatorFactory operatorFactory;
    private final boolean isUseActualParamName;

    private final LoadingCache<Method, Operator> cache = new DoubleCheckCache<Method, Operator>(
        new CacheLoader<Method, Operator>() {
          public Operator load(Method method) {
            MethodDescriptor md = Methods.getMethodDescriptor(daoClass, method, isUseActualParamName);
            if (logger.isInfoEnabled()) {
              logger.info("Initializing operator for {}", ToStringHelper.toString(md));
            }
            Operator operator = operatorFactory.getOperator(md);
            return operator;
          }
        });

    private MangoInvocationHandler(
        Class<?> daoClass,
        DataSourceFactoryGroup dataSourceFactoryGroup,
        PageHandler pageHandler,
        Config config) {
      this.daoClass = daoClass;
      this.isUseActualParamName = config.isUseActualParamName();
      operatorFactory = new OperatorFactory(dataSourceFactoryGroup, pageHandler, config);
    }

    @Override
    protected Object handleInvocation(Object proxy, Method method, Object[] args) throws Throwable {
      Operator operator = getOperator(method);
      return operator.execute(args);
    }

    Operator getOperator(Method method) {
      return cache.get(method);
    }

  }

}
