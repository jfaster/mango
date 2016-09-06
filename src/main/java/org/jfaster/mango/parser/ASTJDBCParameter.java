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

package org.jfaster.mango.parser;

import org.jfaster.mango.binding.BindingParameter;
import org.jfaster.mango.binding.BindingParameterInvoker;
import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.type.TypeHandler;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.jdbc.JdbcType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 不可迭代参数
 *
 * @author ash
 */
public class ASTJDBCParameter extends AbstractRenderableNode implements ParameterBean {

  private BindingParameter bindingParameter;
  private BindingParameterInvoker bindingParameterInvoker;
  private TypeHandler<?> typeHandler;

  public ASTJDBCParameter(int i) {
    super(i);
  }

  public ASTJDBCParameter(Parser p, int i) {
    super(p, i);
  }

  public void init(String str) {
    Pattern p = Pattern.compile(":(\\w+)((\\.\\w+)*)(\\@\\w+)?");
    Matcher m = p.matcher(str);
    if (!m.matches()) {
      throw new IllegalStateException("Can't compile string '" + str + "'");
    }
    String group1 = m.group(1);
    String group2 = m.group(2);
    String group4 = m.group(4);
    String parameterName = group1;
    String propertyPath = Strings.isNotEmpty(group2) ?
        group2.substring(1) :
        "";
    JdbcType jdbcType = null;
    String jdbcTypeStr = null;
    if (group4 != null) {
      try {
        jdbcTypeStr = group4.substring(1).toUpperCase();
        jdbcType = JdbcType.valueOf(jdbcTypeStr);
      } catch (Exception e) {
        throw new IllegalStateException("error jdbc type '" + jdbcTypeStr + "'");
      }
    }
    bindingParameter = BindingParameter.create(parameterName, propertyPath, jdbcType);
  }

  @Override
  public BindingParameter getBindingParameter() {
    return bindingParameter;
  }

  @Override
  public void setBindingParameter(BindingParameter bindingParameter) {
    this.bindingParameter = bindingParameter;
  }

  @Override
  public boolean render(InvocationContext context) {
    if (bindingParameterInvoker == null) {
      throw new NullPointerException("invoker must set");
    }
    context.writeToSqlBuffer("?");
    Object obj = context.getNullableBindingValue(bindingParameterInvoker);
    context.appendToArgs(obj, typeHandler);
    return true;
  }

  @Override
  public String toString() {
    return super.toString() + "{" +
        "fullName=" + getFullName() + ", " +
        "parameterName=" + bindingParameter.getParameterName() + ", " +
        "propertyPath=" + bindingParameter.getPropertyPath() +
        "}";
  }

  @Override
  public Object jjtAccept(ParserVisitor visitor, Object data) {
    return visitor.visit(this, data);
  }

  @Override
  public String getFullName() {
    return bindingParameter.getFullName();
  }

  @Override
  public void setBindingParameterInvoker(BindingParameterInvoker bindingParameterInvoker) {
    this.bindingParameterInvoker = bindingParameterInvoker;
  }

  public void setTypeHandler(TypeHandler<?> typeHandler) {
    this.typeHandler = typeHandler;
  }

}
