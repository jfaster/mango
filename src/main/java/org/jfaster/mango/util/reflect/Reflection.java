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


import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class Reflection {

  public static <T> T instantiate(Class<T> clazz) throws BeanInstantiationException {
    if (clazz.isInterface()) {
      throw new BeanInstantiationException(clazz, "specified class is an interface");
    }
    try {
      return clazz.newInstance();
    } catch (InstantiationException e) {
      throw new BeanInstantiationException(clazz, "Is it an abstract class?", e);
    } catch (IllegalAccessException e) {
      throw new BeanInstantiationException(clazz, "Is the constructor accessible?", e);
    }
  }

  public static <T> T instantiateClass(Class<T> clazz) throws BeanInstantiationException {
    if (clazz.isInterface()) {
      throw new BeanInstantiationException(clazz, "Specified class is an interface");
    }
    try {
      return instantiateClass(clazz.getDeclaredConstructor());
    } catch (NoSuchMethodException ex) {
      throw new BeanInstantiationException(clazz, "No default constructor found", ex);
    }
  }

  public static <T> T instantiateClass(Constructor<T> ctor, Object... args) throws BeanInstantiationException {
    try {
      makeAccessible(ctor);
      return ctor.newInstance(args);
    } catch (InstantiationException ex) {
      throw new BeanInstantiationException(ctor.getDeclaringClass(),
          "Is it an abstract class?", ex);
    } catch (IllegalAccessException ex) {
      throw new BeanInstantiationException(ctor.getDeclaringClass(),
          "Is the constructor accessible?", ex);
    } catch (IllegalArgumentException ex) {
      throw new BeanInstantiationException(ctor.getDeclaringClass(),
          "Illegal arguments for constructor", ex);
    } catch (InvocationTargetException ex) {
      throw new BeanInstantiationException(ctor.getDeclaringClass(),
          "Constructor threw exception", ex.getTargetException());
    }
  }

  public static <T> T newProxy(
      Class<T> interfaceType, InvocationHandler handler) {
    if (!interfaceType.isInterface()) {
      throw new IllegalArgumentException("expected an interface to proxy, but " + interfaceType);
    }
    Object object = Proxy.newProxyInstance(
        interfaceType.getClassLoader(),
        new Class<?>[]{interfaceType},
        handler);
    return interfaceType.cast(object);
  }

  public static void makeAccessible(Constructor<?> ctor) {
    if ((!Modifier.isPublic(ctor.getModifiers()) || !Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) &&
        !ctor.isAccessible()) {
      ctor.setAccessible(true);
    }
  }

  public static Set<Annotation> getAnnotations(Class<?> clazz) {
    Set<Annotation> annos = new HashSet<Annotation>();
    getAnnotations(clazz, annos);
    return annos;
  }

  static void getAnnotations(Class<?> clazz, Set<Annotation> annos) {
    if (clazz == null) {
      return;
    }
    annos.addAll(Arrays.asList(clazz.getDeclaredAnnotations()));
    for (Class<?> parent : clazz.getInterfaces()) {
      getAnnotations(parent, annos);
    }
    getAnnotations(clazz.getSuperclass(), annos);
  }

}
