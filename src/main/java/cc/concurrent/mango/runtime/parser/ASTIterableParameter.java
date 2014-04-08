/*
 * Copyright 2014 mango.concurrent.cc
 *
 * The Netty Project licenses this file to you under the Apache License,
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
import cc.concurrent.mango.jdbc.JdbcUtils;
import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.util.TypeToken;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 可迭代参数
 *
 * @author ash
 */
public class ASTIterableParameter extends ValuableParameter {

    private String interableProperty; // "a in (:1)"中的a

    public ASTIterableParameter(int i) {
        super(i);
    }

    public ASTIterableParameter(Parser p, int i) {
        super(p, i);
    }

    public void setInterableProperty(String interableProperty) {
        this.interableProperty = interableProperty;
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

    public String getInterableProperty() {
        return interableProperty;
    }

    @Override
    public void checkType(TypeContext context) {
        Type type = context.getPropertyType(parameterName, propertyPath);
        TypeToken typeToken = new TypeToken(type);
        Class<?> mappedClass = typeToken.getMappedClass();
        if (!typeToken.isIterable()) { // 不是集合或数组抛出异常
            throw new IncorrectParameterTypeException("invalid type of " + fullName + ", " +
                    "need array or subclass of java.util.Collection but " + type);
        }
        if (mappedClass == null || !JdbcUtils.isSingleColumnClass(mappedClass)) {
            String s = typeToken.isArray() ? "component" : "actual";
            throw new IncorrectParameterTypeException("invalid " + s + " type of " + fullName + ", " +
                    "need a single column class but " + type);
        }
    }

    @Override
    public String toString() {
        return interableProperty + " in (" + fullName + ")";
    }

}
