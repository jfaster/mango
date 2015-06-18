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

import org.jfaster.mango.exception.UnreachableCodeException;
import org.jfaster.mango.invoker.GetterInvokerChain;
import org.jfaster.mango.operator.InvocationContext;
import org.jfaster.mango.util.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 不可迭代参数
 *
 * @author ash
 */
public class ASTJDBCParameter extends AbstractRenderableNode implements ParameterBean {

    private String parameterName;
    private String propertyPath; // 为""的时候表示没有属性
    private GetterInvokerChain invokerChain;

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
            throw new UnreachableCodeException();
        }
        parameterName = m.group(1);
        propertyPath = str.substring(m.end(1));
        if (!propertyPath.isEmpty()) {
            propertyPath = propertyPath.substring(1);  // .property变为property
        }
    }

    @Override
    public boolean render(InvocationContext context) {
        if (invokerChain == null) {
            throw new NullPointerException("invoker must set");
        }
        context.writeToSqlBuffer("?");
        Object obj = context.getNullablePropertyValue(parameterName, invokerChain);
        context.appendToArgs(obj);
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "{" +
                "fullName=" + getFullName() + ", " +
                "parameterName=" + parameterName + ", " +
                "propertyPath=" + propertyPath +
                "}";
    }

    @Override
    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    @Override
    public boolean hasProperty() {
        return Strings.isNotEmpty(propertyPath);
    }

    @Override
    public String getParameterName() {
        return parameterName;
    }

    @Override
    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    @Override
    public String getPropertyPath() {
        return propertyPath;
    }

    @Override
    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }

    @Override
    public String getFullName() {
        return Strings.getFullName(parameterName, propertyPath);
    }

    @Override
    public void setInvokerChain(GetterInvokerChain invokerChain) {
        this.invokerChain = invokerChain;
    }
}
