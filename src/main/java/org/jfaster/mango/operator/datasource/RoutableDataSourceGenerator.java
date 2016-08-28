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

import org.jfaster.mango.binding.BindingParameterInvoker;
import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.datasource.DataSourceFactory;
import org.jfaster.mango.datasource.DataSourceType;
import org.jfaster.mango.sharding.DatabaseShardingStrategy;

import javax.annotation.Nullable;

/**
 * 分库数据源生成器，
 * 使用{@link org.jfaster.mango.annotation.DatabaseShardingBy}或{@link org.jfaster.mango.annotation.ShardingBy}
 * 修饰的参数作为分库参数，
 * 使用{@link org.jfaster.mango.sharding.DatabaseShardingStrategy}作为分库策略，
 * 共同生成分库后数据源
 *
 * @author ash
 */
public class RoutableDataSourceGenerator extends AbstractDataSourceGenerator {

  private final String shardParameterName;
  private final BindingParameterInvoker shardingParameterInvoker;
  private final DatabaseShardingStrategy databaseShardingStrategy;

  public RoutableDataSourceGenerator(DataSourceFactory dataSourceFactory, DataSourceType dataSourceType,
                                     String shardParameterName, BindingParameterInvoker shardingParameterInvoker,
                                     DatabaseShardingStrategy databaseShardingStrategy) {
    super(dataSourceFactory, dataSourceType);
    this.shardParameterName = shardParameterName;
    this.shardingParameterInvoker = shardingParameterInvoker;
    this.databaseShardingStrategy = databaseShardingStrategy;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public String getDatabase(InvocationContext context) {
    Object shardParam = context.getBindingValue(shardingParameterInvoker);
    return databaseShardingStrategy.getDatabase(shardParam);
  }

}
