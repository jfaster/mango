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
import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.invoker.GetterInvokerGroup;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 不可迭代参数
 *
 * @author ash
 */
public class ASTJDBCParameter extends AbstractRenderableNode implements ParameterBean {

    private BindingParameter bindingParameter;
    private GetterInvokerGroup invokerGroup;

    public ASTJDBCParameter(int i) {
        super(i);
    }

    public ASTJDBCParameter(Parser p, int i) {
        super(p, i);
    }

    public void init(String str) {
        Pattern p = Pattern.compile(":(\\w+)(\\.\\w+)*");
        Matcher m = p.matcher(str);
        if (!m.matches()) {
            throw new IllegalStateException("Can't compile string '" + str + "'");
        }
        String parameterName = m.group(1);
        String propertyPath = str.substring(m.end(1));
        if (!propertyPath.isEmpty()) {
            propertyPath = propertyPath.substring(1);  // .property变为property
        }
        bindingParameter = BindingParameter.create(parameterName, propertyPath);
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
        if (invokerGroup == null) {
            throw new NullPointerException("invoker must set");
        }
        context.writeToSqlBuffer("?");
        Object obj = context.getNullablePropertyValue(bindingParameter.getParameterName(), invokerGroup);
        context.appendToArgs(obj);
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
    public void setInvokerGroup(GetterInvokerGroup invokerGroup) {
        this.invokerGroup = invokerGroup;
    }
}
