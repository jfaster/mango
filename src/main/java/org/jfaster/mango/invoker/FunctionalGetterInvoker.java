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
import org.jfaster.mango.exception.UncheckedException;
import org.jfaster.mango.util.reflect.Reflection;
import org.jfaster.mango.util.reflect.TokenTuple;
import org.jfaster.mango.util.reflect.TypeToken;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * 函数式getter方法调用器
 *
 * @author ash
 */
public class FunctionalGetterInvoker extends MethodNamedObject implements GetterInvoker {

  private GetterFunction function;
  private Type returnType;
  private Class<?> returnRawType;

  private FunctionalGetterInvoker(String name, Method method) {
    super(name, method);
    Getter getterAnno = method.getAnnotation(Getter.class);

    TypeToken<?> returnToken = TypeToken.of(method.getGenericReturnType());
    if (getterAnno != null) { // 启用函数式调用功能
      Class<? extends GetterFunction<?, ?>> funcClass = getterAnno.value();
      function = Reflection.instantiateClass(funcClass);

      TokenTuple tokenTuple = TypeToken.of(funcClass).resolveFatherClassTuple(GetterFunction.class);
      TypeToken<?> inputToken = tokenTuple.getFirst();
      TypeToken<?> outputToken = tokenTuple.getSecond();

      TypeToken<?> wrapReturnToken = returnToken.wrap();
      if (!inputToken.isAssignableFrom(wrapReturnToken)) {
        throw new ClassCastException("function[" + function.getClass() + "] " +
            "on method[" + method + "] error, function's inputType[" + inputToken.getType() + "] " +
            "must be assignable from method's returnType[" + returnToken.getType() + "]");
      }
      returnToken = outputToken;
    }
    returnType = returnToken.getType();
    returnRawType = returnToken.getRawType();
  }

  public static FunctionalGetterInvoker create(String name, Method method) {
    return new FunctionalGetterInvoker(name, method);
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object invoke(Object obj) {
    try {
      Object r = method.invoke(obj);
      if (function != null) {
        r = function.apply(r);
      }
      return r;
    } catch (IllegalAccessException e) {
      throw new UncheckedException(e.getMessage(), e.getCause());
    } catch (InvocationTargetException e) {
      throw new UncheckedException(e.getMessage(), e.getCause());
    }
  }

  @Override
  public Type getReturnType() {
    return returnType;
  }

  @Override
  public Class<?> getReturnRawType() {
    return returnRawType;
  }

}
