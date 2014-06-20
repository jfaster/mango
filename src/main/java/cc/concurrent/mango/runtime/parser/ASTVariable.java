/*
 * Copyright 2014 mango.concurrent.cc
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

package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.exception.IncorrectParameterTypeException;
import cc.concurrent.mango.exception.UnreachableCodeException;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * @author ash
 */
public class ASTVariable extends PrimaryExpression {

    private String parameterName;
    private String propertyPath; // 为""的时候表示没有属性
    private String fullName;

    public ASTVariable(int i) {
        super(i);
    }

    public ASTVariable(Parser p, int i) {
        super(p, i);
    }

    public void setParameter(String parameter) {
        Pattern p = Pattern.compile(":(\\w+)(\\.\\w+)*");
        Matcher m = p.matcher(parameter);
        if (!m.matches()) {
            throw new UnreachableCodeException();
        }
        parameterName = m.group(1);
        propertyPath = parameter.substring(m.end(1));
        if (!propertyPath.isEmpty()) {
            propertyPath = propertyPath.substring(1);  // .a.b.c变为a.b.c
        }
        fullName = parameter;
    }

    @Override
    void checkType(TypeContext context) {
        Type type = context.getPropertyType(parameterName, propertyPath);
        Node node = this;
        do {
            node = node.jjtGetParent();
        } while (!(node instanceof ASTExpression) && (node instanceof ASTAddExpression));
        if (node instanceof ASTExpression) { // 到达根节点都是加法
            if (!Integer.class.equals(type) && !int.class.equals(type) && !String.class.equals(type)) {
                throw new IncorrectParameterTypeException("invalid type of " + fullName + ", " +
                        "expected int or java.lang.Integer or java.lang.String but " + type);
            }
        } else { // 到达根节点的途中遇到了非加法
            if (!Integer.class.equals(type) && !int.class.equals(type)) {
                throw new IncorrectParameterTypeException("invalid type of " + fullName + ", " +
                        "expected int or java.lang.Integer but " + type);
            }
        }
    }

    @Override
    public Object value(RuntimeContext context) {
        return context.getNullablePropertyValue(parameterName, propertyPath);
    }

    @Override
    public String toString() {
        return fullName;
    }
}