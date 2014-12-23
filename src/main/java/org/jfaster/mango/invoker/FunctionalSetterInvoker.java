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
import org.jfaster.mango.reflect.Types;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public class FunctionalSetterInvoker extends FunctionalMethod implements SetterInvoker {

    private Class<?> parameterRawType;

    private FunctionalSetterInvoker(Method method) {
        super(method);
        parameterRawType = method.getParameterTypes()[0];
        if (functional) {
            Type parameterType = method.getGenericParameterTypes()[0];
            if (!Types.isTypeAssignable(parameterType, outputType)) {
                throw new RuntimeException(); // TODO;
            }
            parameterRawType = TypeToken.of(parameterRawType).getRawType();
        }
    }

    public static FunctionalSetterInvoker create(Method method) {
        return new FunctionalSetterInvoker(method);
    }

    @Override
    public void invoke(Object object, Object parameter)
            throws IllegalAccessException, InvocationTargetException {
        //noinspection unchecked
        Object output = function.apply(parameter);
        method.invoke(object, output);
    }

    @Override
    public Class<?> getParameterRawType() {
        return parameterRawType;
    }

}
