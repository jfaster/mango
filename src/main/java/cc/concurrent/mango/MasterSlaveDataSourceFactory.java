/*
 * Copyright 2014 mango.concurrent.cc
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

package cc.concurrent.mango;

import cc.concurrent.mango.runtime.operator.SQLType;

import javax.sql.DataSource;
import java.util.List;
import java.util.Random;

/**
 * 主从分离数据源工厂
 *
 * @author ash
 */
public class MasterSlaveDataSourceFactory implements DataSourceFactory {

    private final DataSource master;
    private final List<DataSource> slaves;
    private final Random random = new Random();

    public MasterSlaveDataSourceFactory(DataSource master, List<DataSource> slaves) {
        this.master = master;
        this.slaves = slaves;
    }

    @Override
    public DataSource getDataSource(String name, SQLType sqlType) {
        return sqlType == SQLType.SELECT ? slaves.get(random.nextInt(slaves.size())) : master;
    }

}
