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

package org.jfaster.mango.binding;

import org.jfaster.mango.type.TypeHandler;

import javax.annotation.Nullable;
import java.util.List;

/**
 * @author ash
 */
public interface InvocationContext {

  public void addParameter(String parameterName, Object parameterValue);

  public Object getBindingValue(BindingParameterInvoker invoker);

  @Nullable
  public Object getNullableBindingValue(BindingParameterInvoker invoker);

  public void setBindingValue(BindingParameterInvoker invoker, Object value);

  @Nullable
  public String getGlobalTable();

  public void setGlobalTable(String globalTable);

  public void writeToSqlBuffer(String str);

  public void appendToArgs(Object obj, TypeHandler<?> typeHandler);

  public BoundSql getBoundSql();

  public List<Object> getParameterValues();

}
