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

import org.jfaster.mango.invoker.InvokerCache;
import org.jfaster.mango.invoker.TransferableInvoker;
import org.jfaster.mango.invoker.UnreachablePropertyException;
import org.jfaster.mango.util.reflect.TypeToken;

import java.lang.reflect.Type;

/**
 * @author ash
 */
public class TransferableBindingParameterInvoker implements BindingParameterInvoker {

  private final Type targetType;
  private final BindingParameter bindingParameter;
  private final TransferableInvoker invoker;

  private TransferableBindingParameterInvoker(Type originalType, BindingParameter bindingParameter) {
    Type targetType = originalType;
    TransferableInvoker invoker = null;
    if (bindingParameter.hasProperty()) {
      invoker = InvokerCache.getInvoker(TypeToken.of(targetType).getRawType(),  bindingParameter.getPropertyName());
      targetType = invoker.getColumnType();
    }
    this.targetType = targetType;
    this.bindingParameter = bindingParameter;
    this.invoker = invoker;
  }

  public static TransferableBindingParameterInvoker create(
      Type originalType, BindingParameter bindingParameter) {
    try {
      return new TransferableBindingParameterInvoker(originalType, bindingParameter);
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
    if (invoker != null) {
      if (r == null) {
        throw new BindingException("Parameter '" + bindingParameter.getParameterName() + "' is null");
      }
      r = invoker.invokeGet(obj);
    }
    return r;
  }

  @Override
  public BindingParameter getBindingParameter() {
    return bindingParameter;
  }
}
