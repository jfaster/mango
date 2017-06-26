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

import com.google.common.collect.Sets;
import org.jfaster.mango.util.Strings;
import org.junit.Test;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class BeanUtilTest {

  @Test
  public void fetchPropertyMetas() throws Exception {
    Set<PropertyMeta> pms = Sets.newHashSet(BeanUtil.fetchPropertyMetas(A.class));
    Set<PropertyMeta> set = Sets.newHashSet();
    set.add(getPropertyMeta(A.class, "id", int.class));
    set.add(getPropertyMeta(A.class, "uid", int.class));
    set.add(getPropertyMeta(A.class, "name", String.class));
    assertThat(pms, equalTo(set));
  }

  private PropertyMeta getPropertyMeta(Class<?> clazz, String property, Class<?> type) throws NoSuchMethodException {
    String readMethodName = "get" + Strings.firstLetterToUpperCase(property);
    String writeMethodName = "set" + Strings.firstLetterToUpperCase(property);
    Method readMethod = clazz.getMethod(readMethodName);
    Method writeMethod = clazz.getMethod(writeMethodName, type);
    Field f = tryGetField(clazz, property);
    PropertyMeta pm = new PropertyMeta(
        property, type, readMethod, writeMethod,
        methodAnnos(readMethod), methodAnnos(writeMethod), fieldAnnos(f));
    return pm;
  }

  private Set<Annotation> methodAnnos(Method m) {
    Set<Annotation> annos = new HashSet<Annotation>();
    for (Annotation anno : m.getAnnotations()) {
      annos.add(anno);
    }
    return annos;
  }

  private Set<Annotation> fieldAnnos(Field f) {
    Set<Annotation> annos = new HashSet<Annotation>();
    if (f != null) {
      for (Annotation anno : f.getAnnotations()) {
        annos.add(anno);
      }
    }
    return annos;
  }

  @Nullable
  private Field tryGetField(Class<?> clazz, String name) {
    try {
      return clazz.getDeclaredField(name);
    } catch (Exception e) {
      // ignore
      return null;
    }
  }

}