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

package org.jfaster.mango.datasource;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 数据源工厂组
 *
 * @author ash
 */
public class DataSourceFactoryGroup {

  private Map<String, DataSourceFactory> factoryMap;

  public DataSourceFactoryGroup() {
  }

  public DataSourceFactoryGroup(List<DataSourceFactory> factories) {
    factoryMap = new HashMap<String, DataSourceFactory>();
    for (DataSourceFactory factory : factories) {
      factoryMap.put(factory.getName(), factory);
    }
  }

  public void addDataSourceFactory(DataSourceFactory dataSourceFactory) {
    if (factoryMap == null) {
      factoryMap = new HashMap<String, DataSourceFactory>();
    }
    factoryMap.put(dataSourceFactory.getName(), dataSourceFactory);
  }

  public DataSource getMasterDataSource(String name) {
    DataSourceFactory factory = getDataSourceFactory(name);
    DataSource ds = factory.getMasterDataSource();
    checkDataSourceNotNull(ds, name);
    return ds;
  }

  public DataSource getSlaveDataSource(String name, Class<?> daoClass) {
    DataSourceFactory factory = getDataSourceFactory(name);
    DataSource ds = factory.getSlaveDataSource(daoClass);
    checkDataSourceNotNull(ds, name);
    return ds;
  }

  private DataSourceFactory getDataSourceFactory(String name) {
    DataSourceFactory factory = factoryMap.get(name);
    if (factory == null) {
      throw new IllegalArgumentException("can not find the datasource factory by name [" + name + "], " +
          "available names is " + factoryMap.keySet());
    }
    return factory;
  }

  private void checkDataSourceNotNull(DataSource dataSource, String name) {
    if (dataSource == null) {
      throw new IllegalArgumentException("the datasource fetched by datasource factory is null, " +
          "datasource factory name is [" + name + "]");
    }
  }

}
