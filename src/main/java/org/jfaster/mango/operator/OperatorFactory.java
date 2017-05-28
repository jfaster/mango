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

import org.jfaster.mango.annotation.UseMaster;
import org.jfaster.mango.binding.DefaultParameterContext;
import org.jfaster.mango.binding.InvocationContextFactory;
import org.jfaster.mango.binding.ParameterContext;
import org.jfaster.mango.datasource.DataSourceFactoryGroup;
import org.jfaster.mango.datasource.DataSourceType;
import org.jfaster.mango.descriptor.MethodDescriptor;
import org.jfaster.mango.descriptor.ParameterDescriptor;
import org.jfaster.mango.interceptor.InterceptorChain;
import org.jfaster.mango.interceptor.InvocationInterceptorChain;
import org.jfaster.mango.jdbc.JdbcOperations;
import org.jfaster.mango.jdbc.JdbcTemplate;
import org.jfaster.mango.operator.cache.*;
import org.jfaster.mango.operator.generator.DataSourceGenerator;
import org.jfaster.mango.operator.generator.DataSourceGeneratorFactory;
import org.jfaster.mango.operator.generator.TableGenerator;
import org.jfaster.mango.operator.generator.TableGeneratorFactory;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.parser.SqlParser;
import org.jfaster.mango.stat.MetaStat;
import org.jfaster.mango.util.jdbc.OperatorType;
import org.jfaster.mango.util.jdbc.SQLType;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class OperatorFactory {

  private final CacheHandler cacheHandler;
  private final InterceptorChain interceptorChain;
  private final JdbcOperations jdbcOperations;
  private final Config config;
  private final TableGeneratorFactory tableGeneratorFactory;
  private final DataSourceGeneratorFactory dataSourceGeneratorFactory;

  public OperatorFactory(DataSourceFactoryGroup dataSourceFactoryGroup, CacheHandler cacheHandler,
                         InterceptorChain interceptorChain, Config config) {
    this.cacheHandler = cacheHandler;
    this.interceptorChain = interceptorChain;
    this.config = config;
    this.jdbcOperations = new JdbcTemplate();
    this.tableGeneratorFactory = new TableGeneratorFactory();
    this.dataSourceGeneratorFactory = new DataSourceGeneratorFactory(dataSourceFactoryGroup);
  }

  public AbstractOperator getOperator(MethodDescriptor md, MetaStat stat) {
    ASTRootNode rootNode = SqlParser.parse(md.getSQL()).init(); // 初始化抽象语法树
    List<ParameterDescriptor> pds = md.getParameterDescriptors(); // 方法参数描述
    OperatorType operatorType = getOperatorType(pds, rootNode);
    stat.setOperatorType(operatorType);
    if (operatorType == OperatorType.BATCHUPDATE) { // 批量更新重新组装ParameterDescriptorList
      ParameterDescriptor pd = pds.get(0);
      pds = new ArrayList<ParameterDescriptor>(1);
      pds.add(ParameterDescriptor.create(0, pd.getMappedClass(), pd.getAnnotations(), pd.getName()));
    }

    ParameterContext context = DefaultParameterContext.create(pds);
    rootNode.expandParameter(context); // 扩展简化的参数节点
    rootNode.checkAndBind(context); // 检查类型，设定参数绑定器

    // 构造表生成器
    boolean isSqlUseGlobalTable = !rootNode.getASTGlobalTables().isEmpty();
    TableGenerator tableGenerator = tableGeneratorFactory.getTableGenerator(
        md.getShardingAnno(), md.getGlobalTable(), isSqlUseGlobalTable, context);

    // 构造数据源生成器
    DataSourceType dataSourceType = getDataSourceType(operatorType, md);
    DataSourceGenerator dataSourceGenerator = dataSourceGeneratorFactory.
        getDataSourceGenerator(dataSourceType, md.getShardingAnno(), md.getDataSourceFactoryName(), context);

    AbstractOperator operator;
    if (md.isUseCache()) {
      CacheDriver driver = new CacheDriver(md, rootNode, cacheHandler, context);
      stat.setCacheable(true);
      stat.setUseMultipleKeys(driver.isUseMultipleKeys());
      stat.setCacheNullObject(driver.isCacheNullObject());
      switch (operatorType) {
        case QUERY:
          operator = new CacheableQueryOperator(rootNode, md, driver, config);
          break;
        case UPDATE:
          operator = new CacheableUpdateOperator(rootNode, md, driver, config);
          break;
        case BATCHUPDATE:
          operator = new CacheableBatchUpdateOperator(rootNode, md, driver, config);
          break;
        default:
          throw new IllegalStateException();
      }
    } else {
      switch (operatorType) {
        case QUERY:
          operator = new QueryOperator(rootNode, md, config);
          break;
        case UPDATE:
          operator = new UpdateOperator(rootNode, md, config);
          break;
        case BATCHUPDATE:
          operator = new BatchUpdateOperator(rootNode, md, config);
          break;
        default:
          throw new IllegalStateException();
      }
    }

    InvocationInterceptorChain chain =
        new InvocationInterceptorChain(interceptorChain, pds, rootNode.getSQLType());
    operator.setTableGenerator(tableGenerator);
    operator.setDataSourceGenerator(dataSourceGenerator);
    operator.setInvocationContextFactory(InvocationContextFactory.create(context));
    operator.setInvocationInterceptorChain(chain);
    operator.setJdbcOperations(jdbcOperations);
    return operator;
  }

  OperatorType getOperatorType(List<ParameterDescriptor> pds, ASTRootNode rootNode) {
    OperatorType operatorType;
    if (rootNode.getSQLType() == SQLType.SELECT) {
      operatorType = OperatorType.QUERY;
    } else {
      operatorType = OperatorType.UPDATE;
      if (pds.size() == 1) { // 只有一个参数
        ParameterDescriptor pd = pds.get(0);
        if (pd.isIterable() && rootNode.getJDBCIterableParameters().isEmpty()) {
          // 参数可迭代，同时sql中没有in语句
          operatorType = OperatorType.BATCHUPDATE;
        }
      }
    }
    return operatorType;
  }

  DataSourceType getDataSourceType(OperatorType operatorType, MethodDescriptor md) {
    DataSourceType dataSourceType = DataSourceType.SLAVE;
    if (operatorType != OperatorType.QUERY || md.isAnnotationPresent(UseMaster.class)) {
      dataSourceType = DataSourceType.MASTER;
    }
    return dataSourceType;
  }

}
