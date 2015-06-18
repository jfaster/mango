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

import org.jfaster.mango.annotation.Getter;
import org.jfaster.mango.annotation.Setter;
import org.jfaster.mango.exception.UncheckedException;
import org.jfaster.mango.reflect.Reflection;
import org.jfaster.mango.reflect.TypeToken;
import org.jfaster.mango.reflect.Types;

import javax.annotation.Nullable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author ash
 */
public class FunctionalSetterInvoker extends FunctionalInvoker implements SetterInvoker {

    private SetterFunction function;
    private Type realParameterType;
    private Class<?> realRawParameterType;

    private FunctionalSetterInvoker(String name, Method method) {
        super(name, method);
        TypeToken<?> parameterToken = TypeToken.of(method.getGenericParameterTypes()[0]);
        realParameterType = parameterToken.getType();
        realRawParameterType = parameterToken.getRawType();

        Getter getterAnno = method.getAnnotation(Getter.class);
        if (getterAnno != null) {
            throw new RuntimeException(); // TODO
        }
        Setter setterAnno = method.getAnnotation(Setter.class);

        if (setterAnno != null) { // 启用函数式调用功能
            Class<? extends SetterFunction<?, ?>> funcClass = setterAnno.value();
            function = Reflection.instantiate(funcClass);
            Type genType = funcClass.getGenericSuperclass();
            Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
            TypeToken<?> outputToken = TypeToken.of(params[1]);
            TypeToken<?> wrapParameterToken = parameterToken.wrap();
            if (function.outputTypeIsGeneric()) { // 针对继承GenericSetterFunction的
                if (!outputToken.isAssignableFrom(wrapParameterToken)) {
                    throw new ClassCastException("function[" + function.getClass() + "] " +
                            "on method[" + method + "] error, function's outputType[" + outputToken.getType() + "] " +
                            "must be assignable from method's parameterType[" + parameterToken.getType() + "]");
                }
            } else { // 针对继承LiteSetterFunction的
                if (!wrapParameterToken.isAssignableFrom(outputToken)) {
                    throw new ClassCastException("function[" + function.getClass() + "] " +
                            "on method[" + method + "] error, method's parameterType[" + parameterToken.getType() + "] " +
                            "must be assignable from function's outputType[" + outputToken.getType() + "]");
                }
            }
        }
    }

    public static FunctionalSetterInvoker create(String name, Method method) {
        return new FunctionalSetterInvoker(name, method);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void invoke(Object object, @Nullable Object parameter) {
        try {
            if (function != null) {
                parameter = function.apply(parameter, realParameterType);
            }
            if (parameter == null && realRawParameterType.isPrimitive()) {
                throw new NullPointerException("property " + getName() + " of " +
                        object.getClass() + " is primitive, can not be assigned to null");
            }
            if (parameter != null &&  !Types.isAssignable(realRawParameterType, parameter.getClass())) {
                throw new ClassCastException("cannot convert value of type [" + parameter.getClass().getName() +
                        "] to required type [" + realRawParameterType.getName() + "] " +
                        "for property '" + getName() + "' of " +  object.getClass());
            }
            method.invoke(object, parameter);
        } catch (IllegalAccessException e) {
            throw new UncheckedException(e.getMessage(), e.getCause());
        } catch (InvocationTargetException e) {
            throw new UncheckedException(e.getMessage(), e.getCause());
        }
    }

    @Override
    public Type getType() {
        return null;
    }

    @Override
    public Class<?> getRawType() {
        return null;
    }

}
