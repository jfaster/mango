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

import javax.annotation.concurrent.NotThreadSafe;
import java.lang.reflect.*;
import java.util.HashSet;
import java.util.Set;


@NotThreadSafe
abstract class TypeVisitor {

  private final Set<Type> visited = new HashSet<Type>();

  /**
   * Visits the given types. Null types are ignored. This allows subclasses to call
   * {@code visit(parameterizedType.getOwnerType())} safely without having to check nulls.
   */
  public final void visit(Type... types) {
    for (Type type : types) {
      if (type == null || !visited.add(type)) {
        // null owner type, or already visited;
        continue;
      }
      boolean succeeded = false;
      try {
        if (type instanceof TypeVariable) {
          visitTypeVariable((TypeVariable<?>) type);
        } else if (type instanceof WildcardType) {
          visitWildcardType((WildcardType) type);
        } else if (type instanceof ParameterizedType) {
          visitParameterizedType((ParameterizedType) type);
        } else if (type instanceof Class) {
          visitClass((Class<?>) type);
        } else if (type instanceof GenericArrayType) {
          visitGenericArrayType((GenericArrayType) type);
        } else {
          throw new AssertionError("Unknown type: " + type);
        }
        succeeded = true;
      } finally {
        if (!succeeded) { // When the visitation failed, we don't want to ignore the second.
          visited.remove(type);
        }
      }
    }
  }

  void visitClass(Class<?> t) {
  }

  void visitGenericArrayType(GenericArrayType t) {
  }

  void visitParameterizedType(ParameterizedType t) {
  }

  void visitTypeVariable(TypeVariable<?> t) {
  }

  void visitWildcardType(WildcardType t) {
  }
}
