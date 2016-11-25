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

import org.jfaster.mango.binding.InvocationContextFactory;
import org.jfaster.mango.interceptor.InvocationInterceptorChain;
import org.jfaster.mango.jdbc.JdbcOperations;
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
   * dao操作所在类
   */
  protected final Class<?> daoClass;

  /**
   * 拦截器链
   */
  protected InvocationInterceptorChain invocationInterceptorChain;

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
  protected final ConfigHolder configHolder;

  /**
   * 用于对db进行操作
   */
  protected AbstractOperator(ASTRootNode rootNode, Class<?> daoClass, ConfigHolder configHolder) {
    this.rootNode = rootNode;
    this.daoClass = daoClass;
    this.configHolder = configHolder;
  }

  @Override
  public void setJdbcOperations(JdbcOperations jdbcOperations) {
    this.jdbcOperations = jdbcOperations;
  }

  @Override
  public void setInvocationContextFactory(InvocationContextFactory invocationContextFactory) {
    this.invocationContextFactory = invocationContextFactory;
  }

  @Override
  public void setTableGenerator(TableGenerator tableGenerator) {
    this.tableGenerator = tableGenerator;
  }

  @Override
  public void setDataSourceGenerator(DataSourceGenerator dataSourceGenerator) {
    this.dataSourceGenerator = dataSourceGenerator;
  }

  @Override
  public void setInvocationInterceptorChain(InvocationInterceptorChain invocationInterceptorChain) {
    this.invocationInterceptorChain = invocationInterceptorChain;
  }

}
