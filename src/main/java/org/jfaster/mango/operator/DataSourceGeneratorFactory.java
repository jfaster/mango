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

import org.jfaster.mango.annotation.Sharding;
import org.jfaster.mango.binding.ParameterContext;
import org.jfaster.mango.datasource.DataSourceFactoryGroup;
import org.jfaster.mango.datasource.DataSourceType;

import javax.annotation.Nullable;

/**
 * @author ash
 */
public class DataSourceGeneratorFactory {

  private final DataSourceFactoryGroup dataSourceFactoryGroup;
  private final DatabaseGeneratorFactory databaseGeneratorFactory;

  public DataSourceGeneratorFactory(DataSourceFactoryGroup dataSourceFactoryGroup) {
    this.dataSourceFactoryGroup = dataSourceFactoryGroup;
    this.databaseGeneratorFactory = new DatabaseGeneratorFactory();
  }

  public DataSourceGenerator getDataSourceGenerator(
      DataSourceType dataSourceType, @Nullable Sharding shardingAnno,
      String dataSourceFactoryName, ParameterContext context) {

    DatabaseGenerator databaseGenerator = databaseGeneratorFactory.getDataSourceGenerator(shardingAnno, dataSourceFactoryName, context);
    return new DefaultDataSourceGenerator(databaseGenerator, dataSourceFactoryGroup, dataSourceType);
  }

}
