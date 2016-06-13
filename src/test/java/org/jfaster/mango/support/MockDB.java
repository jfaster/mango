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

package org.jfaster.mango.support;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.sharding.DatabaseShardingStrategy;
import org.jfaster.mango.sharding.NotUseDatabaseShardingStrategy;
import org.jfaster.mango.sharding.NotUseTableShardingStrategy;
import org.jfaster.mango.sharding.TableShardingStrategy;

import java.lang.annotation.Annotation;

/**
 * @author ash
 */
public class MockDB implements Annotation, DB {

    private String database = "";

    private String table = "";

    private Class<? extends TableShardingStrategy> tablePartition = NotUseTableShardingStrategy.class;

    private Class<? extends DatabaseShardingStrategy> dataSourceRouter = NotUseDatabaseShardingStrategy.class;

    public MockDB() {
    }

    public MockDB(String database, String table, Class<? extends TableShardingStrategy> tablePartition,
                  Class<? extends DatabaseShardingStrategy> dataSourceRouter) {
        this.database = database;
        this.table = table;
        this.tablePartition = tablePartition;
        this.dataSourceRouter = dataSourceRouter;
    }

    @Override
    public String database() {
        return database;
    }

    @Override
    public String table() {
        return table;
    }

    @Override
    public Class<? extends TableShardingStrategy> tablePartition() {
        return tablePartition;
    }

    @Override
    public Class<? extends DatabaseShardingStrategy> dataSourceRouter() {
        return dataSourceRouter;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        throw new UnsupportedOperationException();
    }

}
