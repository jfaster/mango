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

/**
 * 简单的单一数据源工厂
 *
 * @author ash
 */
public class SimpleDataSourceFactory extends AbstractDataSourceFactory {

  private DataSource dataSource;

  public SimpleDataSourceFactory() {
  }

  public SimpleDataSourceFactory(DataSource dataSource) {
    this.dataSource = dataSource;
  }

  public SimpleDataSourceFactory(String name, DataSource dataSource) {
    super(name);
    this.dataSource = dataSource;
  }

  @Override
  public DataSource getMasterDataSource() {
    return dataSource;
  }

  @Override
  public DataSource getSlaveDataSource(Class<?> daoClass) {
    return dataSource;
  }

  public DataSource getDataSource() {
    return dataSource;
  }

  public void setDataSource(DataSource dataSource) {
    this.dataSource = dataSource;
  }

}
