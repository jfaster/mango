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

package org.jfaster.mango.operator.generator;

import org.jfaster.mango.annotation.DatabaseShardingBy;
import org.jfaster.mango.annotation.Sharding;
import org.jfaster.mango.annotation.ShardingBy;
import org.jfaster.mango.binding.BindingParameter;
import org.jfaster.mango.binding.BindingParameterInvoker;
import org.jfaster.mango.binding.ParameterContext;
import org.jfaster.mango.datasource.DataSourceFactoryGroup;
import org.jfaster.mango.datasource.DataSourceType;
import org.jfaster.mango.descriptor.ParameterDescriptor;
import org.jfaster.mango.exception.DescriptionException;
import org.jfaster.mango.exception.IncorrectParameterTypeException;
import org.jfaster.mango.sharding.DatabaseShardingStrategy;
import org.jfaster.mango.sharding.NotUseDatabaseShardingStrategy;
import org.jfaster.mango.sharding.NotUseShardingStrategy;
import org.jfaster.mango.util.reflect.Reflection;
import org.jfaster.mango.util.reflect.TypeToken;
import org.jfaster.mango.util.reflect.TypeWrapper;

import javax.annotation.Nullable;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public class DataSourceGeneratorFactory {

  private final DataSourceFactoryGroup dataSourceFactoryGroup;

  public DataSourceGeneratorFactory(DataSourceFactoryGroup dataSourceFactoryGroup) {
    this.dataSourceFactoryGroup = dataSourceFactoryGroup;
  }

  public DataSourceGenerator getDataSourceGenerator(
      DataSourceType dataSourceType, @Nullable Sharding shardingAnno,
      String dataSourceFactoryName, ParameterContext context) {

    DatabaseShardingStrategy strategy = getDatabaseShardingStrategy(shardingAnno);
    TypeToken<?> strategyToken = null;
    if (strategy != null) {
      strategyToken = TypeToken.of(strategy.getClass()).resolveFatherClass(DatabaseShardingStrategy.class);
    }

    int shardingParameterNum = 0;
    String shardingParameterName = null;
    String shardingParameterProperty = null;
    for (ParameterDescriptor pd : context.getParameterDescriptors()) {
      DatabaseShardingBy databaseShardingByAnno = pd.getAnnotation(DatabaseShardingBy.class);
      if (databaseShardingByAnno != null) {
        shardingParameterName = context.getParameterNameByPosition(pd.getPosition());
        shardingParameterProperty = databaseShardingByAnno.value();
        shardingParameterNum++;
        continue; // 有了@DatabaseShardingBy，则忽略@ShardingBy
      }
      ShardingBy shardingByAnno = pd.getAnnotation(ShardingBy.class);
      if (shardingByAnno != null) {
        shardingParameterName = context.getParameterNameByPosition(pd.getPosition());
        shardingParameterProperty = shardingByAnno.value();
        shardingParameterNum++;
      }
    }
    DataSourceGenerator dataSourceGenerator;
    if (strategy != null) {
      if (shardingParameterNum == 1) {
        BindingParameterInvoker shardingParameterInvoker
            = context.getBindingParameterInvoker(BindingParameter.create(shardingParameterName, shardingParameterProperty, null));
        Type shardingParameterType = shardingParameterInvoker.getTargetType();
        TypeWrapper tw = new TypeWrapper(shardingParameterType);
        Class<?> mappedClass = tw.getMappedClass();
        if (mappedClass == null || tw.isIterable()) {
          throw new IncorrectParameterTypeException("the type of parameter Modified @DatabaseShardingBy is error, " +
              "type is " + shardingParameterType + ", " +
              "please note that @ShardingBy = @TableShardingBy + @DatabaseShardingBy");
        }
        TypeToken<?> shardToken = TypeToken.of(shardingParameterType);
        if (!strategyToken.isAssignableFrom(shardToken.wrap())) {
          throw new ClassCastException("DatabaseShardingStrategy[" + strategy.getClass() + "]'s " +
              "generic type[" + strategyToken.getType() + "] must be assignable from " +
              "the type of parameter Modified @DatabaseShardingBy [" + shardToken.getType() + "], " +
              "please note that @ShardingBy = @TableShardingBy + @DatabaseShardingBy");
        }
        dataSourceGenerator = new ShardedDataSourceGenerator(dataSourceFactoryGroup, dataSourceType, shardingParameterInvoker, strategy);
      } else {
        throw new DescriptionException("if @Sharding.databaseShardingStrategy is defined, " +
            "need one and only one @DatabaseShardingBy on method's parameter but found " + shardingParameterNum + ", " +
            "please note that @ShardingBy = @TableShardingBy + @DatabaseShardingBy");
      }
    } else {
      dataSourceGenerator = new SimpleDataSourceGenerator(dataSourceFactoryGroup, dataSourceType, dataSourceFactoryName);
    }
    return dataSourceGenerator;

  }

  @Nullable
  private DatabaseShardingStrategy getDatabaseShardingStrategy(@Nullable Sharding shardingAnno) {
    if (shardingAnno == null) {
      return null;
    }
    Class<? extends DatabaseShardingStrategy> strategyClass = shardingAnno.databaseShardingStrategy();
    if (!strategyClass.equals(NotUseDatabaseShardingStrategy.class)) {
      DatabaseShardingStrategy strategy = Reflection.instantiateClass(strategyClass);
      return strategy;
    }
    strategyClass = shardingAnno.shardingStrategy();
    if (!strategyClass.equals(NotUseShardingStrategy.class)) {
      DatabaseShardingStrategy strategy = Reflection.instantiateClass(strategyClass);
      return strategy;
    }
    return null;
  }

}
