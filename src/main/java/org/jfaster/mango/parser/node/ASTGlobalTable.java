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

package org.jfaster.mango.parser.node;

import org.jfaster.mango.parser.Parser;
import org.jfaster.mango.parser.ParserVisitor;
import org.jfaster.mango.partition.TablePartition;
import org.jfaster.mango.support.RuntimeContext;

/**
 * @author ash
 */
public class ASTGlobalTable extends AbstractRenderableNode {

    private String table; // 原始表名称

    private String shardParameterName;
    private String shardPpropertyPath; // 为""的时候表示没有属性
    private TablePartition tablePartition; // 分表

    public ASTGlobalTable(int i) {
        super(i);
    }

    public ASTGlobalTable(Parser p, int i) {
        super(p, i);
    }

    @Override
    public boolean render(RuntimeContext context) {
        if (table == null) {
            throw new IllegalStateException("please setTable before render");
        }
        String realTable;
        if (!needTablePartition()) {
            realTable = table;
        } else {
            Object shardParam = context.getPropertyValue(shardParameterName, shardPpropertyPath);
            realTable = tablePartition.getPartitionedTable(table, shardParam);
        }
        context.writeToSqlBuffer(realTable);
        return true;
    }

    boolean needTablePartition() {
        return tablePartition != null && shardParameterName != null && shardPpropertyPath != null;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setPartitionInfo(TablePartition tablePartition, String shardParameterName, String shardPpropertyPath) {
        this.tablePartition = tablePartition;
        this.shardParameterName = shardParameterName;
        this.shardPpropertyPath = shardPpropertyPath;
    }

    @Override
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

}
