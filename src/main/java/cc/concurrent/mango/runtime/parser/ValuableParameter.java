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

import cc.concurrent.mango.runtime.RuntimeContext;

/**
 * @author ash
 */
public abstract class ValuableParameter extends ValuableNode {

    protected String parameterName;
    protected String propertyPath; // 为""的时候表示没有属性
    protected String fullName;

    public ValuableParameter(int i) {
        super(i);
    }

    public ValuableParameter(Parser p, int i) {
        super(p, i);
    }

    @Override
    public Object value(RuntimeContext context) {
        return context.getNullablePropertyValue(parameterName, propertyPath);
    }

    @Override
    Token getFirstToken() {
        return jjtGetFirstToken();
    }

    @Override
    Token getLastToken() {
        return jjtGetLastToken();
    }

    public String getParameterName() {
        return parameterName;
    }

    public String getPropertyPath() {
        return propertyPath;
    }
}
