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
import org.jfaster.mango.partition.DataSourceRouter;
import org.jfaster.mango.partition.IgnoreDataSourceRouter;
import org.jfaster.mango.partition.IgnoreTablePartition;
import org.jfaster.mango.partition.TablePartition;

import java.lang.annotation.Annotation;

/**
 * @author ash
 */
public class MockDB implements Annotation, DB {

    private String dataSource = "";

    private String table = "";

    private Class<? extends TablePartition> tablePartition = IgnoreTablePartition.class;

    private Class<? extends DataSourceRouter> dataSourceRouter = IgnoreDataSourceRouter.class;

    public MockDB() {
    }

    public MockDB(String dataSource, String table, Class<? extends TablePartition> tablePartition,
                  Class<? extends DataSourceRouter> dataSourceRouter) {
        this.dataSource = dataSource;
        this.table = table;
        this.tablePartition = tablePartition;
        this.dataSourceRouter = dataSourceRouter;
    }

    @Override
    public String dataSource() {
        return dataSource;
    }

    @Override
    public String table() {
        return table;
    }

    @Override
    public Class<? extends TablePartition> tablePartition() {
        return tablePartition;
    }

    @Override
    public Class<? extends DataSourceRouter> dataSourceRouter() {
        return dataSourceRouter;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
        throw new UnsupportedOperationException();
    }

}
