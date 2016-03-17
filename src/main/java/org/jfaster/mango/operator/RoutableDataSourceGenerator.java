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

import org.jfaster.mango.datasource.DataSourceFactory;
import org.jfaster.mango.datasource.DataSourceType;
import org.jfaster.mango.invoker.GetterInvokerGroup;
import org.jfaster.mango.partition.DataSourceRouter;

import javax.annotation.Nullable;

/**
 * 分库数据源生成器，
 * 使用{@link org.jfaster.mango.annotation.DataSourceShardBy}或{@link org.jfaster.mango.annotation.ShardBy}
 * 修饰的参数作为分库参数，
 * 使用{@link org.jfaster.mango.partition.DataSourceRouter}作为分库策略，
 * 共同生成分库后数据源
 *
 * @author ash
 */
public class RoutableDataSourceGenerator extends AbstractDataSourceGenerator {

    private final String shardParameterName;
    private final GetterInvokerGroup shardByInvokerGroup;
    private final DataSourceRouter dataSourceRouter;

    protected RoutableDataSourceGenerator(DataSourceFactory dataSourceFactory, DataSourceType dataSourceType,
                                          String shardParameterName, GetterInvokerGroup shardByInvokerGroup,
                                          DataSourceRouter dataSourceRouter) {
        super(dataSourceFactory, dataSourceType);
        this.shardParameterName = shardParameterName;
        this.shardByInvokerGroup = shardByInvokerGroup;
        this.dataSourceRouter = dataSourceRouter;
    }

    @Nullable
    @Override
    public String getDataSourceName(InvocationContext context) {
        Object shardParam = context.getPropertyValue(shardParameterName, shardByInvokerGroup);
        return dataSourceRouter.getDataSourceName(shardParam);
    }

}
