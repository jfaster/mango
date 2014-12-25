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
import org.jfaster.mango.invoker.GetterInvoker;
import org.jfaster.mango.operator.InvocationContext;
import org.jfaster.mango.util.Iterables;
import org.jfaster.mango.util.Strings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 可迭代参数
 *
 * @author ash
 */
public class ASTJDBCIterableParameter extends AbstractRenderableNode implements ParameterBean {

    private String name;
    private String property; // 为""的时候表示没有属性
    private GetterInvoker invoker;

    private String propertyOfMapper; // "a in (:1)"中的a

    public ASTJDBCIterableParameter(int i) {
        super(i);
    }

    public ASTJDBCIterableParameter(Parser p, int i) {
        super(p, i);
    }

    public void init(String str) {
        Pattern p = Pattern.compile("in\\s*\\(\\s*(:(\\w+)(\\.\\w+)?)\\s*\\)", Pattern.CASE_INSENSITIVE);
        Matcher m = p.matcher(str);
        if (!m.matches()) {
            throw new UnreachableCodeException();
        }
        String fullName = m.group(1);
        name = m.group(2);
        property = fullName.substring(name.length() + 1);
        if (!property.isEmpty()) {
            property = property.substring(1);  // .property变为property
        }
    }

    @Override
    public boolean render(InvocationContext context) {
        Object objs = context.getNullablePropertyValue(name, invoker);
        if (objs == null) {
            throw new NullPointerException("value of " +
                    Strings.getFullName(name, property) + " can't be null");
        }
        Iterables iterables = new Iterables(objs);
        if (iterables.isEmpty()) {
            throw new IllegalArgumentException("value of " +
                    Strings.getFullName(name, property) + " can't be empty");
        }
        context.writeToSqlBuffer("in (");
        int t = 0;
        for (Object obj : iterables) {
            context.appendToArgs(obj);
            if (t == 0) {
                context.writeToSqlBuffer("?");
            } else {
                context.writeToSqlBuffer(",?");
            }
            t++;
        }
        context.writeToSqlBuffer(")");
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "{" +
                "fullName=" + getFullName() + ", " +
                "name=" + name + ", " +
                "property=" + property +
                "}";
    }

    @Override
    public Object jjtAccept(ParserVisitor visitor, Object data) {
        return visitor.visit(this, data);
    }

    @Override
    public boolean hasProperty() {
        return Strings.isNotEmpty(property);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public void setName(String parameterName) {
        this.name = parameterName;
    }

    @Override
    public String getProperty() {
        return property;
    }

    @Override
    public void setProperty(String property) {
        this.property = property;
    }

    @Override
    public String getFullName() {
        return Strings.getFullName(name, property);
    }

    @Override
    public GetterInvoker getInvoker() {
        return invoker;
    }

    @Override
    public void setInvoker(GetterInvoker invoker) {
        this.invoker = invoker;
    }

    public String getPropertyOfMapper() {
        return propertyOfMapper;
    }

    public void setPropertyOfMapper(String propertyOfMapper) {
        this.propertyOfMapper = propertyOfMapper;
    }

}
