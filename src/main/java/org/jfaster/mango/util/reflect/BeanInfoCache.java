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

import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author ash
 */
public class BeanInfoCache {

    public static Method getReadMethod(Class<?> clazz, String propertyName) {
        return cache.getUnchecked(clazz).getReadMethod(propertyName);
    }

    public static Method getWriteMethod(Class<?> clazz, String propertyName) {
        return cache.getUnchecked(clazz).getWriteMethod(propertyName);
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
        final Map<String, Method> readMethodMap;
        final Map<String, Method> writeMethodMap;

        public BeanInfo(Class<?> clazz) throws Exception {
            Map<String, Method> rmm = new HashMap<String, Method>();
            Map<String, Method> wmm = new HashMap<String, Method>();
            List<PropertyDescriptor> pds = new ArrayList<PropertyDescriptor>();

            java.beans.BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
            for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
                pds.add(pd);
                String name = pd.getName();
                Method readMethod = pd.getReadMethod();
                if (readMethod != null) {
                    rmm.put(name, readMethod);
                }
                Method writeMethod = pd.getWriteMethod();
                if (writeMethod != null) {
                    wmm.put(name, writeMethod);
                }
            }

            propertyDescriptors = Collections.unmodifiableList(pds);
            readMethodMap = Collections.unmodifiableMap(rmm);
            writeMethodMap = Collections.unmodifiableMap(wmm);
        }

        public Method getReadMethod(String propertyName) {
            return readMethodMap.get(propertyName);
        }

        public Method getWriteMethod(String propertyName) {
            return writeMethodMap.get(propertyName);
        }

        public List<PropertyDescriptor> getPropertyDescriptors() {
            return propertyDescriptors;
        }

    }

}
