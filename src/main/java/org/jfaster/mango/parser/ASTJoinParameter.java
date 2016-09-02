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

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ash
 */
public class ASTJoinParameter extends AbstractRenderableNode implements ParameterBean {

  private BindingParameter bindingParameter;
  private BindingParameterInvoker bindingParameterInvoker;

  public ASTJoinParameter(int id) {
    super(id);
  }

  public ASTJoinParameter(Parser p, int id) {
    super(p, id);
  }

  public void init(String str) {
    Pattern p = Pattern.compile("#\\{\\s*(:(\\w+)(\\.\\w+)*)\\s*\\}", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(str);
    if (!m.matches()) {
      throw new IllegalStateException("Can't compile string '" + str + "'");
    }
    String fullName = m.group(1);
    String parameterName = m.group(2);
    String propertyPath = fullName.substring(parameterName.length() + 1);
    if (!propertyPath.isEmpty()) {
      propertyPath = propertyPath.substring(1);  // .property变为property
    }
    bindingParameter = BindingParameter.create(parameterName, propertyPath, null);
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
      throw new NullPointerException("invoker chain must set");
    }
    Object obj = context.getBindingValue(bindingParameterInvoker);
    context.writeToSqlBuffer(obj.toString());
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
}