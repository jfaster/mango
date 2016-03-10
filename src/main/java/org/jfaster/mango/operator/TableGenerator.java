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

import org.jfaster.mango.invoker.GetterInvokerGroup;
import org.jfaster.mango.partition.TablePartition;

/**
 * @author ash
 */
public class TableGenerator {

    private String table; // 原始表名称
    private String shardParameterName;
    private GetterInvokerGroup shardByInvokerGroup;
    private TablePartition tablePartition; // 分表

    public TableGenerator(String table, String shardParameterName,
                          GetterInvokerGroup shardByInvokerGroup, TablePartition tablePartition) {
        this.table = table;
        this.shardParameterName = shardParameterName;
        this.shardByInvokerGroup = shardByInvokerGroup;
        this.tablePartition = tablePartition;
    }

    public String getTable(InvocationContext context) {
        String realTable;
        if (!needTablePartition()) {
            realTable = table;
        } else {
            Object shardParam = context.getPropertyValue(shardParameterName, shardByInvokerGroup);
            realTable = tablePartition.getPartitionedTable(table, shardParam);
        }
        return realTable;
    }

    boolean needTablePartition() {
        return tablePartition != null && shardParameterName != null && shardByInvokerGroup != null;
    }

}
