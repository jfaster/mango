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

package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.TablePartition;
import cc.concurrent.mango.exception.UnreachableCodeException;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.util.TypeToken;

import java.lang.reflect.Type;

/**
 * @author ash
 */
public class ASTTable extends ValuableNode {

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

    String value() {
        if (isDynamicNode()) {
            throw new UnreachableCodeException();
        }
        return table;
    }

    @Override
    Object value(RuntimeContext context) {
        if (!isDynamicNode()) {
            throw new UnreachableCodeException();
        }
        Object shardParam = context.getPropertyValue(shardParameterName, shardPpropertyPath);
        return tablePartition.getPartitionedTable(table, shardParam);
    }

    @Override
    void checkType(TypeContext context) {
        if (isDynamicNode()) {
            Type type = context.getPropertyType(shardParameterName, shardPpropertyPath);
            TypeToken typeToken = new TypeToken(type);
            Class<?> mappedClass = typeToken.getMappedClass();
            if (mappedClass == null || typeToken.isIterable()) {
                throw new RuntimeException(); // TODO
            }
        }
    }

    @Override
    boolean isDynamicNode() {
        return tablePartition != null && shardParameterName != null && shardPpropertyPath != null;
    }

    @Override
    Token getFirstToken() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    Token getLastToken() {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setShardParameterName(String shardParameterName) {
        this.shardParameterName = shardParameterName;
    }

    public void setShardPpropertyPath(String shardPpropertyPath) {
        this.shardPpropertyPath = shardPpropertyPath;
    }

    public void setTablePartition(TablePartition tablePartition) {
        this.tablePartition = tablePartition;
    }
}
