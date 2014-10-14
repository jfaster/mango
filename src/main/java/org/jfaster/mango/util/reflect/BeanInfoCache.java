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

package org.jfaster.mango.util.reflect;

import org.jfaster.mango.util.concurrent.cache.CacheLoader;
import org.jfaster.mango.util.concurrent.cache.DoubleCheckCache;
import org.jfaster.mango.util.concurrent.cache.LoadingCache;

import javax.annotation.Nullable;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author ash
 */
public class BeanInfoCache {

    @Nullable
    public static GetterInvoker getGetterInvoker(Class<?> clazz, String propertyName) {
        return cache.getUnchecked(clazz).getGetterInvoker(propertyName);
    }

    @Nullable
    public static SetterInvoker getSetterInvoker(Class<?> clazz, String propertyName) {
        return cache.getUnchecked(clazz).getSetterInvoker(propertyName);
    }

    public static List<PropertyDescriptor> getPropertyDescriptors(Class<?> clazz) {
        return cache.getUnchecked(clazz).getPropertyDescriptors();
    }

    private final static LoadingCache<Class<?>, BeanInfo> cache = new DoubleCheckCache<Class<?>, BeanInfo>(
            new CacheLoader<Class<?>, BeanInfo>() {
                public BeanInfo load(Class<?> clazz) throws Exception {
                    return new BeanInfo(clazz);
                }
            });

    private static class BeanInfo {

        final List<PropertyDescriptor> propertyDescriptors;
        final Map<String, GetterInvoker> getterInvokerMap;
        final Map<String, SetterInvoker> setterInvokerMap;

        public BeanInfo(Class<?> clazz) throws Exception {
            Map<String, GetterInvoker> gim = new HashMap<String, GetterInvoker>();
            Map<String, SetterInvoker> sim = new HashMap<String, SetterInvoker>();
            List<PropertyDescriptor> pds = new ArrayList<PropertyDescriptor>();

            java.beans.BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                pds.add(pd);
                String name = pd.getName();
                Method readMethod = pd.getReadMethod();
                if (readMethod != null) {
                    gim.put(name, createGetterInvoker(readMethod));
                }
                Method writeMethod = pd.getWriteMethod();
                if (writeMethod != null) {
                    sim.put(name, createSetterInvoker(writeMethod));
                }
            }

            propertyDescriptors = Collections.unmodifiableList(pds);
            getterInvokerMap = Collections.unmodifiableMap(gim);
            setterInvokerMap = Collections.unmodifiableMap(sim);
        }

        public GetterInvoker getGetterInvoker(String propertyName) {
            return getterInvokerMap.get(propertyName);
        }

        public SetterInvoker getSetterInvoker(String propertyName) {
            return setterInvokerMap.get(propertyName);
        }

        public List<PropertyDescriptor> getPropertyDescriptors() {
            return propertyDescriptors;
        }

        private GetterInvoker createGetterInvoker(final Method method) {
            handleModifier(method);

            return new GetterInvoker() {
                @Override
                public Object invoke(Object object) throws IllegalAccessException, InvocationTargetException {
                    return method.invoke(object);
                }

                @Override
                public Type getReturnType() {
                    return method.getGenericReturnType();
                }
            };
        }

        private SetterInvoker createSetterInvoker(final Method method) {
            handleModifier(method);

            return new SetterInvoker() {
                @Override
                public void invoke(Object object, Object parameter)
                        throws IllegalAccessException, InvocationTargetException {
                    method.invoke(object, parameter);
                }

                @Override
                public Class<?> getParameterRawType() {
                    return method.getParameterTypes()[0];
                }
            };
        }

        private void handleModifier(Method method) {
            int modifiers = method.getModifiers();
            if (!Modifier.isPublic(modifiers) ||
                    !Modifier.isPublic(method.getDeclaringClass().getModifiers())) {
                method.setAccessible(true);
            }
        }

    }

}
