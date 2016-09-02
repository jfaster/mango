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
import java.util.*;

/**
 * @author ash
 */
public class DefaultInvocationContext implements InvocationContext {

  private final Map<String, Object> parameterNameToValueMap = new LinkedHashMap<String, Object>();
  private final List<Object> parameterValues = new LinkedList<Object>();
  private final Map<String, Object> cache = new HashMap<String, Object>();

  private final StringBuilder sql = new StringBuilder();
  private final List<Object> args = new LinkedList<Object>();
  private final List<TypeHandler<?>> typeHandlers = new LinkedList<TypeHandler<?>>();

  private String globalTable;

  private DefaultInvocationContext() {
  }

  public static DefaultInvocationContext create() {
    return new DefaultInvocationContext();
  }

  @Override
  public void addParameter(String parameterName, Object parameterValue) {
    parameterNameToValueMap.put(parameterName, parameterValue);
    parameterValues.add(parameterValue);
  }

  @Override
  public Object getBindingValue(BindingParameterInvoker invoker) {
    Object value = getNullableBindingValue(invoker);
    if (value == null) {
      throw new BindingException("Parameter '" + invoker.getBindingParameter() + "' need a non-null value");
    }
    return value;
  }

  @Override
  @Nullable
  public Object getNullableBindingValue(BindingParameterInvoker invoker) {
    String key = getCacheKey(invoker);
    if (cache.containsKey(key)) { // 有可能缓存null对象
      return cache.get(key);
    }
    String parameterName = invoker.getBindingParameter().getParameterName();
    if (!parameterNameToValueMap.containsKey(parameterName)) { // ParameterContext进行过检测，理论上这段代码执行不到
      throw new BindingException("Parameter '" + BindingParameter.create(parameterName, "", null) + "' not found, " +
          "available root parameters are " + transToBindingParameters(parameterNameToValueMap.keySet()));
    }
    Object obj = parameterNameToValueMap.get(parameterName);
    Object value = invoker.invoke(obj);
    cache.put(key, value);
    return value;
  }

  @Override
  public void setBindingValue(BindingParameterInvoker invoker, Object value) {
    String key = getCacheKey(invoker);
    cache.put(key, value);
  }

  @Override
  @Nullable
  public String getGlobalTable() {
    return globalTable;
  }

  @Override
  public void setGlobalTable(String globalTable) {
    this.globalTable = globalTable;
  }

  @Override
  public void writeToSqlBuffer(String str) {
    sql.append(str);
  }

  @Override
  public void appendToArgs(Object obj, TypeHandler<?> typeHandler) {
    args.add(obj);
    typeHandlers.add(typeHandler);
  }

  @Override
  public BoundSql getBoundSql() {
    return new BoundSql(sql.toString(), args, typeHandlers);
  }

  @Override
  public List<Object> getParameterValues() {
    return parameterValues;
  }

  private String getCacheKey(BindingParameterInvoker invoker) {
    return invoker.getBindingParameter().getFullName();
  }

  private Set<BindingParameter> transToBindingParameters(Collection<String> parameterNames) {
    Set<BindingParameter> rs = new LinkedHashSet<BindingParameter>();
    for (String parameterName : parameterNames) {
      rs.add(BindingParameter.create(parameterName, "", null));
    }
    return rs;
  }

}
