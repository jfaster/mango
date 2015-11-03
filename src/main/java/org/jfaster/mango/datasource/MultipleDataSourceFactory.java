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
import java.util.Map;

/**
 * 多数据源工厂
 * <p>
 * 该工厂不能独立使用，需要和{@link SimpleDataSourceFactory}或{@link MasterSlaveDataSourceFactory}一起使用。
 * </p>
 *
 * @author ash
 */
public class MultipleDataSourceFactory implements DataSourceFactory {

    private Map<String, DataSourceFactory> factories;

    public MultipleDataSourceFactory() {
    }

    public MultipleDataSourceFactory(Map<String, DataSourceFactory> factories) {
        this.factories = factories;
    }

    @Override
    public DataSource getMasterDataSource(String dataSourceName) {
        DataSourceFactory factory = getDataSourceFactory(dataSourceName);
        return factory.getMasterDataSource(dataSourceName);
    }

    @Override
    public DataSource getSlaveDataSource(String dataSourceName, Class<?> daoClass) {
        DataSourceFactory factory = getDataSourceFactory(dataSourceName);
        return factory.getSlaveDataSource(dataSourceName, daoClass);
    }

    private DataSourceFactory getDataSourceFactory(String dataSourceName) {
        DataSourceFactory factory = factories.get(dataSourceName);
        if (factory == null) {
            throw new IllegalArgumentException("can not find the data source factory by name [" + dataSourceName + "], " +
                    "available data sources name is " + factories.keySet());
        }
        return factory;
    }

    public Map<String, DataSourceFactory> getFactories() {
        return factories;
    }

    public void setFactories(Map<String, DataSourceFactory> factories) {
        this.factories = factories;
    }

}
