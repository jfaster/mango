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

package org.jfaster.mango.invoker;

import org.jfaster.mango.annotation.Functional;
import org.jfaster.mango.reflect.Reflection;
import org.jfaster.mango.reflect.TypeToken;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public abstract class FunctionalInvoker implements Invoker {

    protected String name;
    protected Method method;
    protected Function function;
    protected TypeToken<?> inputToken;
    protected TypeToken<?> outputToken;

    protected FunctionalInvoker(String name, Method method) {
        this.name = name;
        this.method = method;
        handleMethod(method);
        Functional funcAnno = method.getAnnotation(Functional.class);
        if (funcAnno != null) {
            Class<? extends Function<?, ?>> funcClass = funcAnno.value();
            function = Reflection.instantiate(funcClass);
            Type genType = funcClass.getGenericSuperclass();
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            inputToken = TypeToken.of(params[0]);
            outputToken = TypeToken.of(params[1]);
        } else {
            function = new IdentityFunction();
        }
    }

    @Override
    public String getName() {
        return name;
    }

    protected boolean needCheckAndChange() {
        return !function.isIdentity();
    }

    private void handleMethod(Method method) {
        method.setAccessible(true); // 提高反射速度
    }

}
