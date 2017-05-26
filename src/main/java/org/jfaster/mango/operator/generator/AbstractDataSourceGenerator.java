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

package org.jfaster.mango.operator.generator;

import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.datasource.DataSourceFactoryGroup;
import org.jfaster.mango.datasource.DataSourceType;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import javax.sql.DataSource;

/**
 * @author ash
 */
public abstract class AbstractDataSourceGenerator implements DataSourceGenerator {

  private final static InternalLogger logger = InternalLoggerFactory.getInstance(AbstractDataSourceGenerator.class);

  private final DataSourceFactoryGroup dataSourceFactoryGroup;
  private final DataSourceType dataSourceType;

  protected AbstractDataSourceGenerator(DataSourceFactoryGroup dataSourceFactoryGroup, DataSourceType dataSourceType) {
    this.dataSourceFactoryGroup = dataSourceFactoryGroup;
    this.dataSourceType = dataSourceType;
  }

  @Override
  public DataSource getDataSource(InvocationContext context, Class<?> daoClass) {
    String dataSourceFactoryName = getDataSourceFactoryName(context);
    if (logger.isDebugEnabled()) {
      logger.debug("The name of datasource factory is [" + dataSourceFactoryName + "]");
    }
    DataSource ds = dataSourceType == DataSourceType.MASTER ?
        dataSourceFactoryGroup.getMasterDataSource(dataSourceFactoryName) :
        dataSourceFactoryGroup.getSlaveDataSource(dataSourceFactoryName, daoClass);
    return ds;
  }

  public abstract String getDataSourceFactoryName(InvocationContext context);

}
