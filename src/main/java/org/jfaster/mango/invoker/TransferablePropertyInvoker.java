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

import org.jfaster.mango.annotation.Transfer;
import org.jfaster.mango.exception.UncheckedException;
import org.jfaster.mango.util.ToStringHelper;
import org.jfaster.mango.util.bean.PropertyMeta;
import org.jfaster.mango.util.reflect.Reflection;
import org.jfaster.mango.util.reflect.TokenTuple;
import org.jfaster.mango.util.reflect.TypeToken;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * TODO 优化异常提示
 *
 * @author ash
 */
public class TransferablePropertyInvoker implements TransferableInvoker {

  private final String name;

  private final Method getter;

  private final Method setter;

  private final PropertyTransfer propertyTransfer;

  private final Type columnType;

  private final TypeToken actualPropertyToken;

  private TransferablePropertyInvoker(PropertyMeta propertyMeta) {
    name = propertyMeta.getName();
    getter = propertyMeta.getReadMethod();
    setter = propertyMeta.getWriteMethod();
    actualPropertyToken = TypeToken.of(propertyMeta.getType());
    Transfer transAnno = propertyMeta.getPropertyAnno(Transfer.class);
    if (transAnno != null) {
      Class<? extends PropertyTransfer<?, ?>> transferClass = transAnno.value();
      propertyTransfer = Reflection.instantiateClass(transferClass);
      TokenTuple tokenTuple = TypeToken.of(transferClass).resolveFatherClassTuple(PropertyTransfer.class);
      TypeToken<?> columnToken = tokenTuple.getSecond();
      columnType = columnToken.getType();
      if (propertyTransfer.isCheckType()) {
        Type propertyType = tokenTuple.getFirst().getType();
        if (!propertyType.equals(actualPropertyToken.getType())) {
          throw new ClassCastException(String.format("error transfer<%s, %s> for property type %s",
              ToStringHelper.toString(propertyType), ToStringHelper.toString(columnType), ToStringHelper.toString(actualPropertyToken.getType())));
        }
      }
    } else {
      propertyTransfer = null;
      columnType = propertyMeta.getType();
    }
    handleMethod(getter);
    handleMethod(setter);
  }

  public static TransferablePropertyInvoker create(PropertyMeta propertyMeta) {
    return new TransferablePropertyInvoker(propertyMeta);
  }

  @Override
  public String getName() {
    return name;
  }

  @SuppressWarnings("unchecked")
  @Override
  public Object invokeGet(Object obj) {
    try {
      Object r = getter.invoke(obj);
      if (propertyTransfer != null) {
        r = propertyTransfer.propertyToColumn(r);
      }
      return r;
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new UncheckedException(e.getMessage(), e.getCause());
    }
  }

  @SuppressWarnings("unchecked")
  @Override
  public void invokeSet(Object object, Object columnValue) {
    try {
      Object propertyValue = columnValue;
      if (propertyTransfer != null) {
        propertyValue = propertyTransfer.columnToProperty(columnValue, actualPropertyToken.getType());
      }
      if (propertyValue == null && actualPropertyToken.isPrimitive()) {
        throw new NullPointerException("property " + getName() + " of " +
            object.getClass() + " is primitive, can not be assigned to null");
      }
      setter.invoke(object, propertyValue);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new UncheckedException(e.getMessage(), e.getCause());
    }
  }

  @Override
  public Type getColumnType() {
    return columnType;
  }

  private void handleMethod(Method method) {
    method.setAccessible(true); // 提高反射速度
  }

}
