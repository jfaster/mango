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
import org.jfaster.mango.util.NestedProperty;
import org.jfaster.mango.util.PropertyTokenizer;
import org.jfaster.mango.util.Strings;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class FunctionalGetterInvokerGroup implements GetterInvokerGroup {

    private final Type originalType;
    private final Type targetType;
    private String propertyPath;
    private final List<GetterInvoker> invokers;

    private FunctionalGetterInvokerGroup(Type originalType, String propertyPath) {
        this.originalType = originalType;
        this.propertyPath = propertyPath;
        invokers = new ArrayList<GetterInvoker>();
        Type currentType = originalType;
        Class<?> rawType = TypeToken.of(currentType).getRawType();
        PropertyTokenizer prop = new PropertyTokenizer(propertyPath);
        NestedProperty np = new NestedProperty();
        while (prop.hasCurrent()) {
            String propertyName = prop.getName();
            np.append(propertyName);
            GetterInvoker invoker = InvokerCache.getNullableGetterInvoker(rawType, propertyName);
            if (invoker == null) {
                throw new UnreachablePropertyException(originalType, currentType, propertyName, np.getNestedProperty());
            }
            invokers.add(invoker);
            currentType = invoker.getReturnType();
            rawType = TypeToken.of(currentType).getRawType();
            prop = prop.next();
        }
        targetType = currentType;
    }

    public static FunctionalGetterInvokerGroup create(Type originalType, String propertyPath) {
        return new FunctionalGetterInvokerGroup(originalType, propertyPath);
    }

    @Override
    public Type getOriginalType() {
        return originalType;
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
                    np.append(invokers.get(i).getName());
                }
                String key = i == 0 ? "parameter" : "property";
                // TODO
                String fullName = Strings.getFullName("", np.getNestedProperty());
                throw new NullPointerException(key + " " + fullName + " is null");
            }
            r = invokers.get(i).invoke(r);
        }
        return r;
    }

    @Override
    public String getPropertyPath() {
        return propertyPath;
    }

}
