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

package org.jfaster.mango.util.bean;

import org.jfaster.mango.exception.UncheckedException;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.local.CacheLoader;
import org.jfaster.mango.util.local.DoubleCheckCache;
import org.jfaster.mango.util.local.LoadingCache;

import javax.annotation.Nullable;
import java.beans.BeanInfo;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * 抽取当前类与向上所有被继承类的property，
 * 同时含有get与set方法，才能算一个property
 * get与set方法只含有1个，不算property
 *
 * @author ash
 */
public class BeanUtil {

  private static final int MISS_FLAG = -1;

  private final static LoadingCache<Class<?>, List<PropertyMeta>> cache =
      new DoubleCheckCache<Class<?>, List<PropertyMeta>>(
        new CacheLoader<Class<?>, List<PropertyMeta>>() {
          public List<PropertyMeta> load(Class<?> clazz) {
            try {
              BeanInfo beanInfo = Introspector.getBeanInfo(clazz);
              List<Field> fields = fetchField(clazz);
              TreeMap<Integer, PropertyMeta> metaMap = new TreeMap<Integer, PropertyMeta>();
              PropertyDescriptor[] pds = beanInfo.getPropertyDescriptors();
              int missIndex = fields.size();
              for (PropertyDescriptor pd : pds) {
                Method readMethod = pd.getReadMethod();
                Method writeMethod = pd.getWriteMethod();
                if (readMethod != null && writeMethod != null) {
                  String name = pd.getName();
                  Type type = readMethod.getGenericReturnType(); // 和writeMethod的type相同
                  Field field = tryGetField(readMethod.getDeclaringClass(), name);
                  if (isBoolean(pd.getPropertyType()) && field == null) {
                    String bname = "is" + Strings.firstLetterToUpperCase(name);
                    field = tryGetField(clazz, bname);
                    if (field != null) {
                      name = bname;  // 使用isXxYy替换xxYy
                    }
                  }
                  PropertyMeta meta = new PropertyMeta(name, type, readMethod, writeMethod,
                      methodAnnos(readMethod), methodAnnos(writeMethod), fieldAnnos(field));
                  int index = indexOfFields(field, fields);
                  if (index == MISS_FLAG) {
                    index = missIndex;
                    missIndex++;
                  }
                  metaMap.put(index, meta);
                }
              }
              return transToList(metaMap);
            } catch (Exception e) {
              throw new UncheckedException(e.getMessage(), e);
            }
          }
      });

  public static List<PropertyMeta> fetchPropertyMetas(Class<?> clazz) {
    return cache.get(clazz);
  }

  @Nullable
  public static PropertyMeta fetchPropertyMeta(Class<?> clazz, String name) {
    for (PropertyMeta propertyMeta : fetchPropertyMetas(clazz)) {
      if (propertyMeta.getName().equals(name)) {
        return propertyMeta;
      }
    }
    return null;
  }

  static List<Field> fetchField(Class<?> clazz) {
    List<Field> fields = new LinkedList<Field>();
    fillFields(clazz, fields);
    return fields;
  }

  private static void fillFields(Class<?> clazz, List<Field> fields) {
    if (Object.class.equals(clazz)) {
      return;
    }
    fillFields(clazz.getSuperclass(), fields);
    for (Field field : clazz.getDeclaredFields()) {
      fields.add(field);
    }
  }

  private static boolean isBoolean(Class<?> clazz) {
    return boolean.class.equals(clazz) || Boolean.class.equals(clazz);
  }

  private static int indexOfFields(@Nullable Field field, List<Field> fields) {
    if (field != null) {
      for (int i = 0; i < fields.size(); i++) {
        if (field.equals(fields.get(i))) {
          return i;
        }
      }
    }
    return MISS_FLAG;
  }

  @Nullable
  private static Field tryGetField(Class<?> clazz, String name) {
    try {
      return clazz.getDeclaredField(name);
    } catch (Exception e) {
      // ignore
      return null;
    }
  }

  private static Set<Annotation> methodAnnos(Method m) {
    Set<Annotation> annos = new HashSet<Annotation>();
    for (Annotation anno : m.getAnnotations()) {
      annos.add(anno);
    }
    return annos;
  }

  private static Set<Annotation> fieldAnnos(@Nullable Field f) {
    Set<Annotation> annos = new HashSet<Annotation>();
    if (f != null) {
      for (Annotation anno : f.getAnnotations()) {
        annos.add(anno);
      }
    }
    return annos;
  }

  private static List<PropertyMeta> transToList(TreeMap<Integer, PropertyMeta> metaMap) {
    List<PropertyMeta> metas = new ArrayList<PropertyMeta>();
    for (Integer key : metaMap.keySet()) {
      metas.add(metaMap.get(key));
    }
    return Collections.unmodifiableList(metas);
  }

}
