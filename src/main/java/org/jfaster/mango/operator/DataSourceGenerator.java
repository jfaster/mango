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
import org.jfaster.mango.exception.IncorrectDefinitionException;
import org.jfaster.mango.invoker.GetterInvokerGroup;
import org.jfaster.mango.partition.DataSourceRouter;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import javax.annotation.Nullable;
import javax.sql.DataSource;

/**
 * @author ash
 */
public class DataSourceGenerator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(DataSourceGenerator.class);

    private final DataSourceFactory dataSourceFactory;
    private final DataSourceType dataSourceType;
    private final String dataSourceName;
    private final String shardParameterName;
    private final GetterInvokerGroup shardByInvokerGroup;
    private final DataSourceRouter dataSourceRouter; // 分表

    public DataSourceGenerator(DataSourceFactory dataSourceFactory, DataSourceType dataSourceType,
                               String dataSourceName, String shardParameterName,
                               GetterInvokerGroup shardByInvokerGroup, DataSourceRouter dataSourceRouter) {
        this.dataSourceFactory = dataSourceFactory;
        this.dataSourceType = dataSourceType;
        this.dataSourceName = dataSourceName;
        this.shardParameterName = shardParameterName;
        this.shardByInvokerGroup = shardByInvokerGroup;
        this.dataSourceRouter = dataSourceRouter;
    }

    public DataSource getDataSource(InvocationContext context, Class<?> daoClass) {
        return getDataSource(getDataSourceName(context), daoClass);
    }

    private DataSource getDataSource(String dataSourceName, Class<?> daoClass) {
        if (logger.isDebugEnabled()) {
            logger.debug("The name of Datasource is [" + dataSourceName + "]");
        }
        DataSource ds = dataSourceType == DataSourceType.MASTER ?
                dataSourceFactory.getMasterDataSource(dataSourceName) :
                dataSourceFactory.getSlaveDataSource(dataSourceName, daoClass);
        if (ds == null) {
            throw new IncorrectDefinitionException("can't find datasource for name [" + dataSourceName + "]");
        }
        return ds;
    }

    @Nullable
    public String getDataSourceName(InvocationContext context) {
        String realDataSourceName = dataSourceRouter != null ?
                dataSourceRouter.getDataSourceName(context.getPropertyValue(shardParameterName, shardByInvokerGroup)) :
                dataSourceName;
        return realDataSourceName;
    }

}
