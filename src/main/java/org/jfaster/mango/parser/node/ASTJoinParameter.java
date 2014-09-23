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

package org.jfaster.mango.parser.node;

import org.jfaster.mango.exception.UnreachableCodeException;
import org.jfaster.mango.parser.Parser;
import org.jfaster.mango.parser.ParserVisitor;
import org.jfaster.mango.support.RuntimeContext;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ash
 */
public class ASTJoinParameter extends AbstractRenderableNode {

    private String parameterName;
    private String propertyPath; // 为""的时候表示没有属性
    private String fullName;

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
            throw new UnreachableCodeException();
        }
        fullName = m.group(1);
        parameterName = m.group(2);
        propertyPath = fullName.substring(parameterName.length() + 1);
        if (!propertyPath.isEmpty()) {
            propertyPath = propertyPath.substring(1);  // .a.b.c变为a.b.c
        }
    }

    @Override
    public boolean render(RuntimeContext context) {
        Object obj = context.getPropertyValue(parameterName, propertyPath);
        context.writeToSqlBuffer(obj.toString());
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + "{" + "fullName=" + fullName + ", " +
                "parameterName=" + parameterName + ", " +
                "propertyPath=" + propertyPath + "}";
    }

    @Override
    public Object jjtAccept(ParserVisitor visitor, Object data)
    {
        return visitor.visit(this, data);
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getPropertyPath() {
        return propertyPath;
    }

    public String getFullName() {
        return fullName;
    }
}