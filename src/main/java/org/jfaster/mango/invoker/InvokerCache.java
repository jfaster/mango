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

import org.jfaster.mango.exception.UncheckedException;
import org.jfaster.mango.util.bean.BeanUtil;
import org.jfaster.mango.util.bean.PropertyMeta;
import org.jfaster.mango.util.local.CacheLoader;
import org.jfaster.mango.util.local.DoubleCheckCache;
import org.jfaster.mango.util.local.LoadingCache;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author ash
 */
public class InvokerCache {

  @Nullable
  public static GetterInvoker getNullableGetterInvoker(Class<?> clazz, String propertyName) {
    return cache.get(clazz).getGetterInvoker(propertyName);
  }

  public static GetterInvoker getGetterInvoker(Class<?> clazz, String propertyName) {
    GetterInvoker invoker = getNullableGetterInvoker(clazz, propertyName);
    if (invoker == null) {
      throw new UnreachablePropertyException("There is no getter/setter for property named '" + propertyName + "' in '" + clazz + "'");
    }
    return invoker;
  }

  public static List<GetterInvoker> getGetterInvokers(Class<?> clazz) {
    return cache.get(clazz).getGetterInvokers();
  }

  @Nullable
  public static SetterInvoker getNullableSetterInvoker(Class<?> clazz, String propertyName) {
    return cache.get(clazz).getSetterInvoker(propertyName);
  }

  public static SetterInvoker getSetterInvoker(Class<?> clazz, String propertyName) {
    SetterInvoker invoker = cache.get(clazz).getSetterInvoker(propertyName);
    if (invoker == null) {
      throw new UnreachablePropertyException("There is no getter/setter for property named '" + propertyName + "' in '" + clazz + "'");
    }
    return invoker;
  }

  public static List<SetterInvoker> getSetterInvokers(Class<?> clazz) {
    return cache.get(clazz).getSetterInvokers();
  }

  private final static LoadingCache<Class<?>, InvokerInfo> cache = new DoubleCheckCache<Class<?>, InvokerInfo>(
      new CacheLoader<Class<?>, InvokerInfo>() {
        public InvokerInfo load(Class<?> clazz) {
          try {
            return new InvokerInfo(clazz);
          } catch (Exception e) {
            throw new UncheckedException(e.getMessage(), e);
          }
        }
      });

  private static class InvokerInfo {

    private final Map<String, GetterInvoker> getterInvokerMap;
    private final Map<String, SetterInvoker> setterInvokerMap;
    private final List<GetterInvoker> getterInvokers;
    private final List<SetterInvoker> setterInvokers;

    public InvokerInfo(Class<?> clazz) throws Exception {
      Map<String, GetterInvoker> gim = new HashMap<String, GetterInvoker>();
      Map<String, SetterInvoker> sim = new HashMap<String, SetterInvoker>();
      List<GetterInvoker> gis = new ArrayList<GetterInvoker>();
      List<SetterInvoker> sis = new ArrayList<SetterInvoker>();

      for (PropertyMeta pm : BeanUtil.fetchPropertyMetas(clazz)) {
        String name = pm.getName();
        Method readMethod = pm.getReadMethod();
        Method writeMethod = pm.getWriteMethod();
        // TODO 注解传入
        FunctionalGetterInvoker fgi = FunctionalGetterInvoker.create(name, readMethod);
        gim.put(name, fgi);
        gis.add(fgi);
        FunctionalSetterInvoker fsi = FunctionalSetterInvoker.create(name, writeMethod);
        sim.put(name, fsi);
        sis.add(fsi);
      }
      getterInvokerMap = Collections.unmodifiableMap(gim);
      setterInvokerMap = Collections.unmodifiableMap(sim);
      getterInvokers = Collections.unmodifiableList(gis);
      setterInvokers = Collections.unmodifiableList(sis);
    }

    GetterInvoker getGetterInvoker(String propertyName) {
      return getterInvokerMap.get(propertyName);
    }

    SetterInvoker getSetterInvoker(String propertyName) {
      return setterInvokerMap.get(propertyName);
    }

    private List<GetterInvoker> getGetterInvokers() {
      return getterInvokers;
    }

    private List<SetterInvoker> getSetterInvokers() {
      return setterInvokers;
    }

    private static boolean isBoolean(Class<?> clazz) {
      return boolean.class.equals(clazz) || Boolean.class.equals(clazz);
    }

  }

}
