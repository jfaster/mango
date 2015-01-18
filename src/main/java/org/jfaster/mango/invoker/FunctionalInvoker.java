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

import java.lang.reflect.*;

/**
 * @author ash
 */
public abstract class FunctionalInvoker implements Invoker {

    protected String name;
    protected MethodInvoker methodInvoker;
    protected Function function;
    protected TypeToken<?> inputToken;
    protected TypeToken<?> outputToken;

    protected FunctionalInvoker(String name, Method method) {
        this.name = name;
        this.methodInvoker = createMethodInvoker(method);
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

    private MethodInvoker createMethodInvoker(final Method method) {

        // We can't use FastMethod if the method is private.
        int modifiers = method.getModifiers();
        if (!Modifier.isPrivate(modifiers) && !Modifier.isProtected(modifiers)) {
            /*if[AOP]*/
            try {
                final net.sf.cglib.reflect.FastMethod fastMethod =
                        BytecodeGen.newFastClass(method.getDeclaringClass(),
                                BytecodeGen.Visibility.forMember(method)).getMethod(method);

                return new MethodInvoker() {
                    public Object invoke(Object target, Object... parameters)
                            throws IllegalAccessException, InvocationTargetException {
                        return fastMethod.invoke(target, parameters);
                    }
                };
            } catch (net.sf.cglib.core.CodeGenerationException e) {/* fall-through */}
            /*end[AOP]*/
        }

        if (!Modifier.isPublic(modifiers) ||
                !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
            method.setAccessible(true);
        }

        return new MethodInvoker() {
            public Object invoke(Object target, Object... parameters)
                    throws IllegalAccessException, InvocationTargetException {
                return method.invoke(target, parameters);
            }
        };
    }

    interface MethodInvoker {
        Object invoke(Object target, Object... parameters)
                throws IllegalAccessException, InvocationTargetException;
    }

}
