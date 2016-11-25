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
import org.jfaster.mango.stat.OneExecuteStat;

/**
 * db操作接口
 *
 * @author ash
 */
public interface Operator {

  public Object execute(Object[] values, OneExecuteStat stat);

  public void setJdbcOperations(JdbcOperations jdbcOperations);

  public void setInvocationContextFactory(InvocationContextFactory invocationContextFactory);

  public void setTableGenerator(TableGenerator tableGenerator);

  public void setDataSourceGenerator(DataSourceGenerator dataSourceGenerator);

  public void setInvocationInterceptorChain(InvocationInterceptorChain invocationInterceptorChain);

}
