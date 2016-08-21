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

import org.jfaster.mango.util.NestedProperty;
import org.jfaster.mango.util.PropertyTokenizer;

/**
 * @author ash
 */
public class FunctionalSetterInvokerGroup implements SetterInvokerGroup {

    private final Class<?> originalType;
    private final String propertyPath;
    private final Class<?> targetType;

    private FunctionalSetterInvokerGroup(Class<?> originalType, String propertyPath) {
        this.originalType = originalType;
        this.propertyPath = propertyPath;
        PropertyTokenizer prop = new PropertyTokenizer(propertyPath);
        if (!prop.hasNext()) {
            throw new IllegalStateException("property path '" + propertyPath + "' error");
        }
        Class<?> currentType = originalType;
        NestedProperty np = new NestedProperty();
        while (prop.hasCurrent()) {
            String propertyName = prop.getName();
            np.append(propertyName);
            SetterInvoker setter = InvokerCache.getNullableSetterInvoker(currentType, propertyName);
            if (setter == null) { // 没有set方法直接异常
                throw new UnreachablePropertyException(originalType, currentType, propertyName, np.getNestedProperty());
            }
            if (prop.hasNext()) { // 后续还有属性则需检测set方法
                GetterInvoker getter = InvokerCache.getNullableGetterInvoker(currentType, propertyName);
                if (getter == null || !setter.getParameterType().equals(getter.getReturnType())) { // 有set方法，但get方法不对，抛出异常
                    throw new UnreachablePropertyException(originalType, currentType, propertyName, np.getNestedProperty());
                }
            }
            currentType = setter.getParameterRawType();
            prop = prop.next();
        }
        targetType = currentType;
    }

    public static FunctionalSetterInvokerGroup create(Class<?> originalType, String propertyPath) {
        return new FunctionalSetterInvokerGroup(originalType, propertyPath);
    }

    @Override
    public Class<?> getOriginalType() {
        return originalType;
    }

    @Override
    public Class<?> getTargetType() {
        return targetType;
    }

    @Override
    public void invoke(Object obj,  Object value) {
        MetaObject mo = MetaObject.forObject(obj);
        mo.setValue(propertyPath, value);
    }

}
