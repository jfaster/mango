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

package org.jfaster.mango.runtime.parser;

import org.jfaster.mango.TablePartition;
import org.jfaster.mango.exception.UnreachableCodeException;
import org.jfaster.mango.runtime.RuntimeContext;

import javax.annotation.Nullable;

/**
 * @author ash
 */
public class ASTTable extends AbstractNode {

    private String table; // 原始表名称

    private String shardParameterName;
    private String shardPpropertyPath; // 为""的时候表示没有属性
    private TablePartition tablePartition; // 分表

    public ASTTable(int i) {
        super(i);
    }

    public ASTTable(Parser p, int i) {
        super(p, i);
    }

    String getTable() {
        if (needTablePartition()) {
            throw new UnreachableCodeException();
        }
        return table;
    }

    String getTable(RuntimeContext context) {
        if (!needTablePartition()) {
            throw new UnreachableCodeException();
        }
        Object shardParam = context.getPropertyValue(shardParameterName, shardPpropertyPath);
        return tablePartition.getPartitionedTable(table, shardParam);
    }

    boolean needTablePartition() {
        return tablePartition != null && shardParameterName != null && shardPpropertyPath != null;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setShardParameterName(@Nullable String shardParameterName) {
        this.shardParameterName = shardParameterName;
    }

    public void setShardPpropertyPath(@Nullable String shardPpropertyPath) {
        this.shardPpropertyPath = shardPpropertyPath;
    }

    public void setTablePartition(@Nullable TablePartition tablePartition) {
        this.tablePartition = tablePartition;
    }
}
