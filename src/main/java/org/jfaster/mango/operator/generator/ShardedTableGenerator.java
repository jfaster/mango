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

import org.jfaster.mango.binding.BindingParameterInvoker;
import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.sharding.TableShardingStrategy;

import javax.annotation.Nullable;

/**
 * 分表表名生成器，以从{@link org.jfaster.mango.annotation.DB#table()}取得的表名作为原始表名，
 * 使用{@link org.jfaster.mango.annotation.TableShardingBy}或{@link org.jfaster.mango.annotation.ShardingBy}
 * 修饰的参数作为分表参数，
 * 使用{@link org.jfaster.mango.sharding.TableShardingStrategy}作为分表策略，
 * 共同生成分表后表名
 *
 * @author ash
 */
public class ShardedTableGenerator implements TableGenerator {

  private final String table; // 原始表名称
  private final BindingParameterInvoker bindingParameterInvoker; // 绑定参数执行器
  private final TableShardingStrategy tableShardingStrategy; // 分表策略

  public ShardedTableGenerator(
      String table, BindingParameterInvoker bindingParameterInvoker, TableShardingStrategy tableShardingStrategy) {
    this.table = table;
    this.bindingParameterInvoker = bindingParameterInvoker;
    this.tableShardingStrategy = tableShardingStrategy;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  @Override
  public String getTable(InvocationContext context) {
    Object shardParam = context.getBindingValue(bindingParameterInvoker);
    return tableShardingStrategy.getTargetTable(table, shardParam);
  }

}
