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

package org.jfaster.mango.operator.table;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.Sharding;
import org.jfaster.mango.annotation.ShardingBy;
import org.jfaster.mango.annotation.TableShardingBy;
import org.jfaster.mango.base.Strings;
import org.jfaster.mango.binding.BindingParameter;
import org.jfaster.mango.binding.BindingParameterInvoker;
import org.jfaster.mango.binding.ParameterContext;
import org.jfaster.mango.exception.IncorrectParameterTypeException;
import org.jfaster.mango.operator.IncorrectDefinitionException;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.reflect.Reflection;
import org.jfaster.mango.reflect.TypeToken;
import org.jfaster.mango.reflect.TypeWrapper;
import org.jfaster.mango.reflect.descriptor.MethodDescriptor;
import org.jfaster.mango.reflect.descriptor.ParameterDescriptor;
import org.jfaster.mango.sharding.NotUseShardingStrategy;
import org.jfaster.mango.sharding.NotUseTableShardingStrategy;
import org.jfaster.mango.sharding.TableShardingStrategy;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public class TableGeneratorFactory {

  public TableGenerator getTableGenerator(
      MethodDescriptor md, ASTRootNode rootNode, ParameterContext context) {
    DB dbAnno = md.getAnnotation(DB.class);
    if (dbAnno == null) {
      throw new IllegalStateException("dao interface expected one @DB " +
          "annotation but not found");
    }
    String table = null;
    if (Strings.isNotEmpty(dbAnno.table())) {
      table = dbAnno.table();
    }

    TableShardingStrategy strategy = getTableShardingStrategy(md);
    TypeToken<?> strategyToken = null;
    if (strategy != null) {
      strategyToken = TypeToken.of(strategy.getClass()).resolveFatherClass(TableShardingStrategy.class);
    }

    // 是否配置使用全局表
    boolean isUseGlobalTable = table != null;

    // 是否配置使用表切分
    boolean isUseTableShardingStrategy = strategy != null;

    // 是否在SQL中使用#table全局表
    boolean isSqlUseGlobalTable = !rootNode.getASTGlobalTables().isEmpty();

    if (isSqlUseGlobalTable && !isUseGlobalTable) {
      throw new IncorrectDefinitionException("if sql use global table '#table'," +
          " @DB.table must be defined");
    }
    if (isUseTableShardingStrategy && !isUseGlobalTable) {
      throw new IncorrectDefinitionException("if @Sharding.tableShardingStrategy is defined, " +
          "@DB.table must be defined");
    }

    int shardingParameterNum = 0;
    String shardingParameterName = null;
    String shardingParameterProperty = null;
    for (ParameterDescriptor pd : md.getParameterDescriptors()) {
      TableShardingBy tableShardingByAnno = pd.getAnnotation(TableShardingBy.class);
      if (tableShardingByAnno != null) {
        shardingParameterName = context.getParameterNameByPosition(pd.getPosition());
        shardingParameterProperty = tableShardingByAnno.value();
        shardingParameterNum++;
        continue; // 有了@TableShardingBy，则忽略@ShardingBy
      }
      ShardingBy shardingByAnno = pd.getAnnotation(ShardingBy.class);
      if (shardingByAnno != null) {
        shardingParameterName = context.getParameterNameByPosition(pd.getPosition());
        shardingParameterProperty = shardingByAnno.value();
        shardingParameterNum++;
      }
    }
    TableGenerator tableGenerator;
    if (isUseTableShardingStrategy) {
      if (shardingParameterNum == 1) {
        BindingParameterInvoker shardingParameterInvoker
            = context.getBindingParameterInvoker(BindingParameter.create(shardingParameterName, shardingParameterProperty));
        Type shardingParameterType = shardingParameterInvoker.getTargetType();
        TypeWrapper tw = new TypeWrapper(shardingParameterType);
        Class<?> mappedClass = tw.getMappedClass();
        if (mappedClass == null || tw.isIterable()) {
          throw new IncorrectParameterTypeException("the type of parameter Modified @TableShardingBy is error, " +
              "type is " + shardingParameterType + ", " +
              "please note that @ShardingBy = @TableShardingBy + @DatabaseShardingBy");
        }
        TypeToken<?> shardToken = TypeToken.of(shardingParameterType);
        if (!strategyToken.isAssignableFrom(shardToken.wrap())) {
          throw new ClassCastException("TableShardingStrategy[" + strategy.getClass() + "]'s " +
              "generic type[" + strategyToken.getType() + "] must be assignable from " +
              "the type of parameter Modified @TableShardingBy [" + shardToken.getType() + "], " +
              "please note that @ShardingBy = @TableShardingBy + @DatabaseShardingBy");
        }
        tableGenerator = new PartitionalTableGenerator(table, shardingParameterName, shardingParameterInvoker, strategy);
      } else {
        throw new IncorrectDefinitionException("if @Sharding.tableShardingStrategy is defined, " +
            "need one and only one @TableShardingBy on method's parameter but found " + shardingParameterNum + ", " +
            "please note that @ShardingBy = @TableShardingBy + @DatabaseShardingBy");
      }
    } else {
      tableGenerator = new SimpleTableGenerator(table);
    }
    return tableGenerator;
  }

  @Nullable
  private TableShardingStrategy getTableShardingStrategy(MethodDescriptor md) {
    Sharding shardingAnno = md.getAnnotation(Sharding.class);
    if (shardingAnno == null) {
      return null;
    }
    Class<? extends TableShardingStrategy> strategyClass = shardingAnno.tableShardingStrategy();
    if (!strategyClass.equals(NotUseTableShardingStrategy.class)) {
      TableShardingStrategy strategy = Reflection.instantiateClass(strategyClass);
      return strategy;
    }
    strategyClass = shardingAnno.shardingStrategy();
    if (!strategyClass.equals(NotUseShardingStrategy.class)) {
      TableShardingStrategy strategy = Reflection.instantiateClass(strategyClass);
      return strategy;
    }
    return null;
  }

}
