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

import org.jfaster.mango.util.Objects;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.List;

/**
 * 方法返回描述
 *
 * @author ash
 */
public class ReturnDescriptor extends TypeWithAnnotationDescriptor {

  private ReturnDescriptor(Type type, List<Annotation> annotations) {
    super(type, annotations);
  }

  public static ReturnDescriptor create(Type type, List<Annotation> annotations) {
    return new ReturnDescriptor(type, annotations);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final ReturnDescriptor other = (ReturnDescriptor) obj;
    return Objects.equal(this.getType(), other.getType())
        && Objects.equal(this.getAnnotations(), other.getAnnotations());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getType(), getAnnotations());
  }

}
