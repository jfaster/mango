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

package org.jfaster.mango.descriptor;

import org.jfaster.mango.util.reflect.Reflection;
import org.jfaster.mango.util.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * @author ash
 */
public class Methods {

  private static final TypeToken<?> genericTypeToken = TypeToken.of(Generic.class);

  public static MethodDescriptor getMethodDescriptor(Class<?> daoClass, Method method, boolean isUseActualParamName) {
    List<Annotation> mas = new LinkedList<Annotation>();
    for (Annotation a : method.getAnnotations()) {
      mas.add(a);
    }
    for (Annotation a : Reflection.getAnnotations(daoClass)) {
      mas.add(a);
    }

    TypeToken<?> daoTypeToken = TypeToken.of(daoClass);
    Type returnType = fixAndResolveType(method.getGenericReturnType(), daoTypeToken);
    ReturnDescriptor rd = ReturnDescriptor.create(returnType, mas);

    List<ParameterDescriptor> pds = new LinkedList<ParameterDescriptor>();
    Type[] genericParameterTypes = method.getGenericParameterTypes();
    Annotation[][] parameterAnnotations = method.getParameterAnnotations();
    String[] names = getParameterNames(method, isUseActualParamName);
    for (int i = 0; i < genericParameterTypes.length; i++) {
      Type type = fixAndResolveType(genericParameterTypes[i], daoTypeToken);
      Annotation[] pas = parameterAnnotations[i];
      String name = names[i];
      pds.add(ParameterDescriptor.create(i, type, Arrays.asList(pas), name));
    }

    return MethodDescriptor.create(method.getName(), daoClass, rd, pds);
  }

  public static List<Method> listMethods(Class<?> clazz) {
    Method[] allMethods = clazz.getMethods();
    List<Method> methods = new ArrayList<Method>();
    for (Method method : allMethods) {
      if (!isDefault(method)) {
        methods.add(method);
      }
    }
    return methods;
  }

  static Type fixAndResolveType(Type type, TypeToken<?> daoTypeToken) {
    type = fixTypeInJava6(type);
    return resolveType(type, daoTypeToken);
  }

  /**
   * java6中，利用反射获得int[]，Integer[]等类型的genericType时，可能会得到泛型数组，
   * 下面的方法会将泛型数组转为class
   */
  static Type fixTypeInJava6(Type type) {
    if (type instanceof GenericArrayType) {
      GenericArrayType gat = (GenericArrayType) type;
      Type componentType = gat.getGenericComponentType();
      if (componentType instanceof Class) {
        return Array.newInstance((Class) componentType, 0).getClass();
      }
    }
    return type;
  }

  static Type resolveType(Type type, TypeToken<?> daoTypeToken) {
    return genericTypeToken.isAssignableFrom(daoTypeToken) ?
        daoTypeToken.resolveType(type).getType() :
        type;
  }

  private static String[] getParameterNames(Method method, boolean isUseActualParamName) {
    String[] names = new String[method.getGenericParameterTypes().length];
    for (int i = 0; i < names.length; i++) {
      String name = null;
      if (isUseActualParamName) {
        name = ParamNameResolver.getActualParamName(method, i);
      }
      if (name == null) {
        name = String.valueOf(i + 1);
      }
      names[i] = name;
    }
    return names;
  }

  private static boolean isDefault(Method m) {
    // Default methods are public non-abstract instance methods
    // declared in an interface.
    return ((m.getModifiers() & (Modifier.ABSTRACT | Modifier.PUBLIC | Modifier.STATIC)) ==
        Modifier.PUBLIC) && m.getDeclaringClass().isInterface();
  }

}
