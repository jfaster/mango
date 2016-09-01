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
 * 方法参数描述
 *
 * @author ash
 */
public class ParameterDescriptor extends TypeWithAnnotationDescriptor {

  /**
   * 此参数在method参数列表中的位置，从0开始
   */
  private final int position;

  /**
   * 此参数在emthod参数列表中的名字
   */
  private final String name;

  private ParameterDescriptor(int position, Type type, List<Annotation> annotations, String name) {
    super(type, annotations);
    this.position = position;
    this.name = name;
  }

  public static ParameterDescriptor create(int position, Type type, List<Annotation> annotations, String name) {
    return new ParameterDescriptor(position, type, annotations, name);
  }

  public int getPosition() {
    return position;
  }

  public String getName() {
    return name;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final ParameterDescriptor other = (ParameterDescriptor) obj;
    return Objects.equal(this.position, other.position)
        && Objects.equal(this.name, other.name)
        && Objects.equal(this.getType(), other.getType())
        && Objects.equal(this.getAnnotations(), other.getAnnotations());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(position, name, getType(), getAnnotations());
  }

}
