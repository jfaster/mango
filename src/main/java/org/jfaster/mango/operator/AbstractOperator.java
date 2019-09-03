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

import org.jfaster.mango.annotation.UseTransactionForBatchUpdate;
import org.jfaster.mango.binding.InvocationContextFactory;
import org.jfaster.mango.descriptor.MethodDescriptor;
import org.jfaster.mango.jdbc.JdbcOperations;
import org.jfaster.mango.operator.generator.DataSourceGenerator;
import org.jfaster.mango.operator.generator.TableGenerator;
import org.jfaster.mango.page.InvocationPageHandler;
import org.jfaster.mango.parser.ASTRootNode;

/**
 * @author ash
 */
public abstract class AbstractOperator implements Operator {

  /**
   * 渲染sql的树节点
   */
  protected final ASTRootNode rootNode;

  /**
   * 方法描述
   */
  protected final MethodDescriptor methodDescriptor;

  /**
   * 全局表
   */
  protected TableGenerator tableGenerator;

  /**
   * 数据源
   */
  protected DataSourceGenerator dataSourceGenerator;

  /**
   * 运行时环境工厂
   */
  protected InvocationContextFactory invocationContextFactory;

  /**
   * jdbc操作
   */
  protected JdbcOperations jdbcOperations;

  /**
   * mango全局配置信息
   */
  protected final Config config;

  /**
   * 用于对db进行操作
   */
  protected AbstractOperator(ASTRootNode rootNode, MethodDescriptor md, Config config) {
    this.rootNode = rootNode;
    this.methodDescriptor = md;
    this.config = config.copy();
    mergeConfig(md);
  }

  @Override
  public MethodDescriptor getMethodDescriptor() {
    return methodDescriptor;
  }

  public void setJdbcOperations(JdbcOperations jdbcOperations) {
    this.jdbcOperations = jdbcOperations;
  }

  public void setInvocationContextFactory(InvocationContextFactory invocationContextFactory) {
    this.invocationContextFactory = invocationContextFactory;
  }

  public void setTableGenerator(TableGenerator tableGenerator) {
    this.tableGenerator = tableGenerator;
  }

  public void setDataSourceGenerator(DataSourceGenerator dataSourceGenerator) {
    this.dataSourceGenerator = dataSourceGenerator;
  }

  private void mergeConfig(MethodDescriptor md) {
    UseTransactionForBatchUpdate anno = md.getAnnotation(UseTransactionForBatchUpdate.class);
    if (anno != null) {
      config.setUseTransactionForBatchUpdate(anno.value());
    }
  }

}
