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

import org.jfaster.mango.reflect.TypeToken;

import java.lang.reflect.Method;

/**
 * @author ash
 */
public abstract class FunctionalInvoker implements Invoker {

    protected String name;
    protected Method method;
    private TypeToken<?> inputToken;
    private TypeToken<?> outputToken;

    protected FunctionalInvoker(String name, Method method) {
        this.name = name;
        this.method = method;
        handleMethod(method);
//        Function function = getFunction();
//        if (function != null) {
//            Type genType = function.getClass().getGenericSuperclass();
//            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
//            inputToken = TypeToken.of(params[0]);
//            outputToken = TypeToken.of(params[1]);
//        }
//
//
//        if (funcAnno != null) {
//            Class<? extends Function<?, ?>> funcClass = funcAnno.value();
//            function = Reflection.instantiate(funcClass);
//
//        } else {
//            function = new IdentityFunction();
//        }
    }

    @Override
    public String getName() {
        return name;
    }

    private void handleMethod(Method method) {
        method.setAccessible(true); // 提高反射速度
    }

}
