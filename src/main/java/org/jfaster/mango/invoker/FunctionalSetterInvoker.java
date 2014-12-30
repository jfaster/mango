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

import com.google.common.reflect.TypeToken;
import org.jfaster.mango.exception.UncheckedException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public class FunctionalSetterInvoker extends FunctionalInvoker implements SetterInvoker {

    private TypeToken<?> parameterToken;
    private TypeToken<?> realParameterToken;
    private Class<?> realParameterRawType;

    private FunctionalSetterInvoker(String name, Method method) {
        super(name, method);
        parameterToken = realParameterToken = TypeToken.of(method.getGenericParameterTypes()[0]);
        realParameterRawType = realParameterToken.getRawType();
        if (needCheckAndChange()) {
            TypeToken<?> wrapParameterToken = parameterToken.wrap();
            if (function.inverseCheck()) { // 针对继承GenericFunction的
                if (!outputToken.isAssignableFrom(wrapParameterToken)) {
                    throw new RuntimeException(); // TODO
                }
            } else { // 针对继承LiteFunction的
                if (!wrapParameterToken.isAssignableFrom(outputToken)) {
                    throw new ClassCastException("function[" + function.getClass() + "] " +
                            "on method[" + method + "] error, method's parameterType[" + parameterToken.getType() + "] " +
                            "must be assignable from function's outputType[" + outputToken.getType() + "]");
                }
            }
            parameterToken = inputToken;
        }
    }

    public static FunctionalSetterInvoker create(String name, Method method) {
        return new FunctionalSetterInvoker(name, method);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void invoke(Object object, Object parameter) {
        try {
            Object output = function.apply(parameter, realParameterToken);
            if (output == null && realParameterRawType.isPrimitive()) {
                throw new NullPointerException("property " + getName() + " of " +
                        object.getClass() + " is primitive, can not be assigned to null");
            }
            // TODO
//            if (output != null &&  !Types.isAssignable(realParameterRawType, output.getClass())) {
//                throw new ClassCastException("cannot convert value of type [" + output.getClass().getName() +
//                        "] to required type [" + realParameterRawType.getName() + "] " +
//                        "for property '" + getName() + "' of " +  object.getClass());
//            }
            method.invoke(object, output);
        } catch (IllegalAccessException e) {
            throw new UncheckedException(e.getMessage(), e.getCause());
        } catch (InvocationTargetException e) {
            throw new UncheckedException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Type getType() {
        return parameterToken.getType();
    }

    @Override
    public Class<?> getRawType() {
        return parameterToken.getRawType();
    }

}
