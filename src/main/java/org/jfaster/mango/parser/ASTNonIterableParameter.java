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

import org.jfaster.mango.exception.IncorrectParameterTypeException;
import org.jfaster.mango.exception.UnreachableCodeException;
import org.jfaster.mango.jdbc.JdbcUtils;
import org.jfaster.mango.support.TypeContext;
import org.jfaster.mango.util.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * 不可迭代参数
 *
 * @author ash
 */
public class ASTNonIterableParameter extends ValuableParameter {


    public ASTNonIterableParameter(int i) {
        super(i);
    }

    public ASTNonIterableParameter(Parser p, int i) {
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
    public void checkType(TypeContext context) {
        Type type = context.getPropertyType(parameterName, propertyPath);
        TypeToken typeToken = new TypeToken(type);
        Class<?> mappedClass = typeToken.getMappedClass();
        if (mappedClass == null || typeToken.isIterable() || !JdbcUtils.isSingleColumnClass(mappedClass)) {
            throw new IncorrectParameterTypeException("invalid type of " + fullName + ", " +
                    "expected a class can be identified by jdbc but " + type);
        }
    }

    @Override
    public String toString() {
        return fullName;
    }

}
