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

package org.jfaster.mango.binding;

import org.jfaster.mango.invoker.GetterInvoker;
import org.jfaster.mango.invoker.InvokerCache;
import org.jfaster.mango.invoker.UnreachablePropertyException;
import org.jfaster.mango.util.NestedProperty;
import org.jfaster.mango.util.PropertyTokenizer;
import org.jfaster.mango.util.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class FunctionalBindingParameterInvoker implements BindingParameterInvoker {

  private final Type targetType;
  private final BindingParameter bindingParameter;
  private final List<GetterInvoker> invokers;

  private FunctionalBindingParameterInvoker(Type originalType, BindingParameter bindingParameter) {
    this.bindingParameter = bindingParameter;
    invokers = new ArrayList<GetterInvoker>();
    Type currentType = originalType;
    Class<?> rawType = TypeToken.of(currentType).getRawType();
    PropertyTokenizer prop = new PropertyTokenizer(bindingParameter.getPropertyPath());
    while (prop.hasCurrent()) {
      String propertyName = prop.getName();
      GetterInvoker invoker = InvokerCache.getGetterInvoker(rawType, propertyName);
      invokers.add(invoker);
      currentType = invoker.getReturnType();
      rawType = TypeToken.of(currentType).getRawType();
      prop = prop.next();
    }
    targetType = currentType;
  }

  public static FunctionalBindingParameterInvoker create(
      Type originalType, BindingParameter bindingParameter) {
    try {
      FunctionalBindingParameterInvoker invokerGroup = new FunctionalBindingParameterInvoker(originalType, bindingParameter);
      return invokerGroup;
    } catch (UnreachablePropertyException e) {
      throw new BindingException("Parameter '" + bindingParameter + "' can't be readable", e);
    }
  }

  @Override
  public Type getTargetType() {
    return targetType;
  }

  @Override
  public Object invoke(Object obj) {
    Object r = obj;
    int size = invokers.size();
    for (int i = 0; i < size; i++) {
      if (r == null) {
        NestedProperty np = new NestedProperty();
        for (int j = 0; j < i; j++) {
          np.append(invokers.get(j).getName());
        }
        BindingParameter bp = BindingParameter.create(bindingParameter.getParameterName(), np.getNestedProperty(), null);
        throw new BindingException("Parameter '" + bp + "' is null");
      }
      r = invokers.get(i).invoke(r);
    }
    return r;
  }

  @Override
  public BindingParameter getBindingParameter() {
    return bindingParameter;
  }
}
