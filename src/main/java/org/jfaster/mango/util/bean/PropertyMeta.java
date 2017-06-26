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

import org.jfaster.mango.util.Objects;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Set;

/**
 * @author ash
 */
public class PropertyMeta {

  private final String name;

  private final Type type;

  private final Method readMethod;

  private final Method writeMethod;

  private final Set<Annotation> readMethodAnnos;

  private final Set<Annotation> writeMethodAnnos;

  private final Set<Annotation> propertyAnnos;

  public PropertyMeta(
      String name, Type type, Method readMethod, Method writeMethod,
      Set<Annotation> readMethodAnnos, Set<Annotation> writeMethodAnnos,
      Set<Annotation> propertyAnnos) {
    this.name = name;
    this.type = type;
    this.readMethod = readMethod;
    this.writeMethod = writeMethod;
    this.readMethodAnnos = readMethodAnnos;
    this.writeMethodAnnos = writeMethodAnnos;
    this.propertyAnnos = propertyAnnos;
  }

  public String getName() {
    return name;
  }

  public Type getType() {
    return type;
  }

  public Method getReadMethod() {
    return readMethod;
  }

  public Method getWriteMethod() {
    return writeMethod;
  }

  @Nullable
  public <T extends Annotation> T getReadMethodAnno(Class<T> annotationType) {
    for (Annotation anno : readMethodAnnos) {
      if (annotationType.isInstance(anno)) {
        return annotationType.cast(anno);
      }
    }
    return null;
  }

  @Nullable
  public <T extends Annotation> T getWriteMethodAnno(Class<T> annotationType) {
    for (Annotation anno : writeMethodAnnos) {
      if (annotationType.isInstance(anno)) {
        return annotationType.cast(anno);
      }
    }
    return null;
  }

  @Nullable
  public <T extends Annotation> T getPropertyAnno(Class<T> annotationType) {
    for (Annotation anno : propertyAnnos) {
      if (annotationType.isInstance(anno)) {
        return annotationType.cast(anno);
      }
    }
    return null;
  }

  @Override
  public boolean equals(Object obj) {
    if (!(obj instanceof PropertyMeta)) {
      return false;
    }
    PropertyMeta other = (PropertyMeta) obj;
    return Objects.equal(this.name, other.name) &&
        Objects.equal(this.type, other.type) &&
        Objects.equal(this.readMethod, other.readMethod) &&
        Objects.equal(this.writeMethod, other.writeMethod) &&
        Objects.equal(this.readMethodAnnos, other.readMethodAnnos) &&
        Objects.equal(this.writeMethodAnnos, other.writeMethodAnnos) &&
        Objects.equal(this.propertyAnnos, other.propertyAnnos);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(name, type, readMethod, writeMethod, readMethodAnnos, writeMethodAnnos, propertyAnnos);
  }
}
