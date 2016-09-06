/**
 *    Copyright 2009-2015 the original author or authors.
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */
package org.jfaster.mango.mapper;

import org.jfaster.mango.invoker.GetterInvoker;
import org.jfaster.mango.invoker.InvokerCache;
import org.jfaster.mango.invoker.SetterInvoker;
import org.jfaster.mango.util.PropertyTokenizer;
import org.jfaster.mango.util.reflect.Reflection;

/**
 * @author Clinton Begin
 * @author ash
 */
public class MetaObject {

  private Object originalObject;

  private Class<?> originalClass;

  private MetaObject(Object object) {
    this.originalObject = object;
    this.originalClass = object.getClass();
  }

  public static MetaObject forObject(Object object) {
    if (object == null) {
      return SystemMetaObject.NULL_META_OBJECT;
    } else {
      return new MetaObject(object);
    }
  }

  public Object getOriginalObject() {
    return originalObject;
  }

  private Object getValue(String propertyName) {
    GetterInvoker invoker = InvokerCache.getGetterInvoker(originalClass, propertyName);
    return invoker.invoke(originalObject);
  }

  public void setValue(String propertyPath, Object value) {
    PropertyTokenizer prop = new PropertyTokenizer(propertyPath);
    if (prop.hasNext()) {
      MetaObject metaValue = metaObjectForProperty(prop.getName());
      if (metaValue == SystemMetaObject.NULL_META_OBJECT) {
        if (value == null && prop.getChildren() != null) {
          // don't instantiate child path if value is null
          return;
        } else {
          SetterInvoker invoker = InvokerCache.getSetterInvoker(originalClass, prop.getName());
          Class<?> clazz = invoker.getParameterRawType();
          Object newObject = Reflection.instantiate(clazz);
          metaValue = MetaObject.forObject(newObject);
          invoker.invoke(originalObject, newObject);
        }
      }
      metaValue.setValue(prop.getChildren(), value);
    } else {
      SetterInvoker invoker = InvokerCache.getSetterInvoker(originalClass, propertyPath);
      invoker.invoke(originalObject, value);
    }
  }

  public MetaObject metaObjectForProperty(String name) {
    Object value = getValue(name);
    return MetaObject.forObject(value);
  }

}
