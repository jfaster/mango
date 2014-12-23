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

import org.jfaster.mango.reflect.Types;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public class FunctionalGetterInvoker extends FunctionalMethod implements GetterInvoker {

    private Type returnType;

    private FunctionalGetterInvoker(Method method) {
        super(method);
        returnType = method.getGenericReturnType();
        if (functional) {
            if (!Types.isTypeAssignable(inputType, returnType)) {
                throw new RuntimeException(); // TODO;
            }
            returnType = outputType;
        }
    }

    public static FunctionalGetterInvoker create(Method method) {
        return new FunctionalGetterInvoker(method);
    }

    @Override
    public Object invoke(Object object) throws IllegalAccessException, InvocationTargetException {
        Object input = method.invoke(object);
        //noinspection unchecked
        Object r = function.apply(input);
        return r;
    }

    @Override
    public Type getReturnType() {
        return returnType;
    }

}
