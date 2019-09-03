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

package org.jfaster.mango.page;

import org.jfaster.mango.binding.BoundSql;
import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.descriptor.ParameterDescriptor;
import org.jfaster.mango.jdbc.JdbcOperations;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class InvocationPageHandler {

  private final PageHandler pageHandler;

  private final List<ParameterDescriptor> parameterDescriptors;

  public InvocationPageHandler(PageHandler pageHandler,
                               List<ParameterDescriptor> parameterDescriptors) {
    this.pageHandler = pageHandler;
    this.parameterDescriptors = parameterDescriptors;
  }

  public void process(BoundSql boundSql, InvocationContext context,
                        JdbcOperations jdbcOperations, DataSource dataSource) {
    List<Object> parameterValues = context.getParameterValues();
    List<Parameter> parameters = new ArrayList<Parameter>(parameterValues.size());
    for (int i = 0; i < parameterValues.size(); i++) {
      ParameterDescriptor pd = parameterDescriptors.get(i);
      parameters.add(new Parameter(pd, parameterValues.get(i)));
    }
    pageHandler.process(boundSql, parameters, jdbcOperations, dataSource);
  }

}
