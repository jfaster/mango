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

import org.jfaster.mango.annotation.Cache;
import org.jfaster.mango.annotation.CacheIgnored;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.base.Config;
import org.jfaster.mango.base.Strings;
import org.jfaster.mango.base.sql.OperatorType;
import org.jfaster.mango.base.sql.SQLType;
import org.jfaster.mango.binding.DefaultParameterContext;
import org.jfaster.mango.binding.InvocationContextFactory;
import org.jfaster.mango.binding.ParameterContext;
import org.jfaster.mango.datasource.DataSourceFactory;
import org.jfaster.mango.exception.DescriptionException;
import org.jfaster.mango.interceptor.InterceptorChain;
import org.jfaster.mango.interceptor.InvocationInterceptorChain;
import org.jfaster.mango.jdbc.JdbcOperations;
import org.jfaster.mango.operator.cache.*;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.parser.SqlParser;
import org.jfaster.mango.reflect.descriptor.MethodDescriptor;
import org.jfaster.mango.reflect.descriptor.ParameterDescriptor;
import org.jfaster.mango.stat.StatsCounter;

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

  public OperatorFactory(DataSourceFactory dataSourceFactory, CacheHandler cacheHandler,
                         InterceptorChain interceptorChain, JdbcOperations jdbcOperations, Config config) {
    this.cacheHandler = cacheHandler;
    this.interceptorChain = interceptorChain;
    this.jdbcOperations = jdbcOperations;
    this.config = config;
    this.tableGeneratorFactory = new TableGeneratorFactory();
    this.dataSourceGeneratorFactory = new DataSourceGeneratorFactory(dataSourceFactory);
  }

  public Operator getOperator(MethodDescriptor md, StatsCounter statsCounter) {
    ASTRootNode rootNode = SqlParser.parse(getSQL(md)).init(); // 初始化抽象语法树
    List<ParameterDescriptor> pds = md.getParameterDescriptors(); // 方法参数描述
    OperatorType operatorType = getOperatorType(pds, rootNode);
    statsCounter.setOperatorType(operatorType);
    if (operatorType == OperatorType.BATCHUPDATE) { // 批量更新重新组装ParameterDescriptorList
      ParameterDescriptor pd = pds.get(0);
      pds = new ArrayList<ParameterDescriptor>(1);
      pds.add(ParameterDescriptor.create(0, pd.getMappedClass(), pd.getAnnotations(), pd.getName()));
    }

    ParameterContext context = DefaultParameterContext.create(pds);
    rootNode.expandParameter(context); // 扩展简化的参数节点
    rootNode.checkAndBind(context); // 检查类型，设定参数绑定器

    // 构造表生成器
    TableGenerator tableGenerator = tableGeneratorFactory.getTableGenerator(md, rootNode, context);

    // 构造数据源生成器
    DataSourceGenerator dataSourceGenerator = dataSourceGeneratorFactory.getDataSourceGenerator(operatorType, md, context);

    Operator operator;
    if (isUseCache(md)) {
      CacheDriver driver = new CacheDriver(md, rootNode, cacheHandler, context, statsCounter);
      statsCounter.setCacheable(true);
      statsCounter.setUseMultipleKeys(driver.isUseMultipleKeys());
      statsCounter.setCacheNullObject(driver.isCacheNullObject());
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
    operator.setStatsCounter(statsCounter);
    return operator;
  }

  String getSQL(MethodDescriptor md) {
    SQL sqlAnno = md.getAnnotation(SQL.class);
    if (sqlAnno == null) {
      throw new DescriptionException("each method expected one @SQL annotation but not found");
    }
    String sql = sqlAnno.value();
    if (Strings.isEmpty(sql)) {
      throw new DescriptionException("sql is null or empty");
    }
    return sql;
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

  private boolean isUseCache(MethodDescriptor md) {
    CacheIgnored cacheIgnoredAnno = md.getAnnotation(CacheIgnored.class);
    Cache cacheAnno = md.getAnnotation(Cache.class);
    return cacheAnno != null && cacheIgnoredAnno == null;
  }

}
