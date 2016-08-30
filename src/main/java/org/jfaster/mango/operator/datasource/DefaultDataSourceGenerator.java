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

package org.jfaster.mango.operator.datasource;

import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;
import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.datasource.DataSourceFactory;
import org.jfaster.mango.datasource.DataSourceType;
import org.jfaster.mango.exception.DescriptionException;
import org.jfaster.mango.operator.datasource.database.DatabaseGenerator;

import javax.sql.DataSource;

/**
 * @author ash
 */
public class DefaultDataSourceGenerator implements DataSourceGenerator {

  private final static InternalLogger logger = InternalLoggerFactory.getInstance(DefaultDataSourceGenerator.class);

  private final DatabaseGenerator databaseGenerator;
  private final DataSourceFactory dataSourceFactory;
  private final DataSourceType dataSourceType;

  public DefaultDataSourceGenerator(
      DatabaseGenerator databaseGenerator, DataSourceFactory dataSourceFactory, DataSourceType dataSourceType) {
    this.databaseGenerator = databaseGenerator;
    this.dataSourceFactory = dataSourceFactory;
    this.dataSourceType = dataSourceType;
  }

  @Override
  public DataSource getDataSource(InvocationContext context, Class<?> daoClass) {
    String database = databaseGenerator.getDatabase(context);
    if (logger.isDebugEnabled()) {
      logger.debug("The name of database is [" + database + "]");
    }
    DataSource ds = dataSourceType == DataSourceType.MASTER ?
        dataSourceFactory.getMasterDataSource(database) :
        dataSourceFactory.getSlaveDataSource(database, daoClass);
    if (ds == null) {
      throw new DescriptionException("can't find database for name [" + database + "]");
    }
    return ds;
  }

}
