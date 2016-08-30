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

package org.jfaster.mango.util;

import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class ToStringHelper {

  public static String toString(Method m) {
    StringBuffer sb = new StringBuffer();
    sb.append(m.getDeclaringClass().getSimpleName()).append(".").append(m.getName()).append("(");
    printTypes(sb, m.getGenericParameterTypes(), "", ", ", "");
    return sb.append(")").toString();
  }

  public static String toString(Type type) {
    StringBuffer sb = new StringBuffer();
    printType(sb, type);
    return sb.toString();
  }

  public static String toString(Iterable<Class> clazzs) {
    List<String> ss = new ArrayList<String>();
    for (Class clazz : clazzs) {
      ss.add(clazz.getSimpleName());
    }
    return ss.toString();
  }

  public static void printTypes(StringBuffer sb, Type[] types, String pre, String sep, String suf) {
    if (types.length > 0) {
      sb.append(pre);
    }
    for (int i = 0; i < types.length; i++) {
      if (i > 0) {
        sb.append(sep);
      }
      printType(sb, types[i]);
    }
    if (types.length > 0) {
      sb.append(suf);
    }
  }

  private static void printType(StringBuffer sb, Type type) {
    if (type instanceof Class) {
      Class t = (Class) type;
      sb.append(t.getSimpleName());
    } else if (type instanceof TypeVariable) {
      TypeVariable t = (TypeVariable) type;
      sb.append(t.getName());
      printTypes(sb, t.getBounds(), " extends ", " & ", "");
    } else if (type instanceof WildcardType) {
      WildcardType t = (WildcardType) type;
      sb.append("?");
      printTypes(sb, t.getLowerBounds(), " extends ", " & ", "");
      printTypes(sb, t.getUpperBounds(), " super ", " & ", "");
    } else if (type instanceof ParameterizedType) {
      ParameterizedType t = (ParameterizedType) type;
      Type owner = t.getOwnerType();
      if (owner != null) {
        printType(sb, owner);
        sb.append(".");
      }
      printType(sb, t.getRawType());
      printTypes(sb, t.getActualTypeArguments(), "<", ", ", ">");
    } else if (type instanceof GenericArrayType) {
      GenericArrayType t = (GenericArrayType) type;
      sb.append("");
      printType(sb, t.getGenericComponentType());
      sb.append("[]");
    }
  }


}
