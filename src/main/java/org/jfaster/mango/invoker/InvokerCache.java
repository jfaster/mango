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
import org.jfaster.mango.util.local.DoubleCheckCache;
import org.jfaster.mango.util.local.LoadingCache;

import java.util.*;

/**
 * @author ash
 */
public class InvokerCache {

  public static TransferableInvoker getInvoker(Class<?> clazz, String propertyName) {
    TransferableInvoker invoker = cache.get(clazz).getInvoker(propertyName);
    if (invoker == null) {
      throw new UnreachablePropertyException("There is no getter/setter for property named '" + propertyName + "' in '" + clazz + "'");
    }
    return invoker;
  }

  public static List<TransferableInvoker> getInvokers(Class<?> clazz) {
    return cache.get(clazz).getInvokers();
  }

  private final static LoadingCache<Class<?>, InvokerInfo> cache = new DoubleCheckCache<>(
      clazz -> {
        try {
          return new InvokerInfo(clazz);
        } catch (Exception e) {
          throw new UncheckedException(e.getMessage(), e);
        }
      });

  private static class InvokerInfo {

    private final Map<String, TransferableInvoker> invokerMap;
    private final List<TransferableInvoker> invokers;

    InvokerInfo(Class<?> clazz) {
      Map<String, TransferableInvoker> tim = new HashMap<>();
      List<TransferableInvoker> tis = new ArrayList<>();

      for (PropertyMeta pm : BeanUtil.fetchPropertyMetas(clazz)) {
        String name = pm.getName();
        TransferableInvoker invoker = TransferablePropertyInvoker.create(pm);
        tim.put(name, invoker);
        tis.add(invoker);
      }
      invokerMap = Collections.unmodifiableMap(tim);
      invokers = Collections.unmodifiableList(tis);
    }

    TransferableInvoker getInvoker(String propertyName) {
      return invokerMap.get(propertyName);
    }

    List<TransferableInvoker> getInvokers() {
      return invokers;
    }

  }

}
