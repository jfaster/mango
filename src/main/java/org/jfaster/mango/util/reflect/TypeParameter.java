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

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;

/**
 * @author ash
 */
public abstract class TypeParameter<T> extends TypeCapture<T> {

  final TypeVariable<?> typeVariable;

  protected TypeParameter() {
    Type type = capture();
    if (!(type instanceof TypeVariable)) {
      throw new IllegalArgumentException("type should be a type variable, but " + type);
    }
    this.typeVariable = (TypeVariable<?>) type;
  }

  @Override public final int hashCode() {
    return typeVariable.hashCode();
  }

  @Override public final boolean equals(@Nullable Object o) {
    if (o instanceof TypeParameter) {
      TypeParameter<?> that = (TypeParameter<?>) o;
      return typeVariable.equals(that.typeVariable);
    }
    return false;
  }

  @Override public String toString() {
    return typeVariable.toString();
  }
}
