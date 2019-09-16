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

package org.jfaster.mango.binding;

import org.jfaster.mango.util.Objects;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.jdbc.JdbcType;

/**
 * @author ash
 */
public class BindingParameter {

  private final String parameterName;

  private final String propertyName;

  private final JdbcType jdbcType;

  public BindingParameter(String parameterName, String propertyName, JdbcType jdbcType) {
    this.parameterName = parameterName;
    this.propertyName = propertyName;
    this.jdbcType = jdbcType;
  }

  public static BindingParameter create(String parameterName, String propertyPath, JdbcType jdbcType) {
    return new BindingParameter(parameterName, propertyPath, jdbcType);
  }

  public BindingParameter rightShift() {
    String newPropertyName = hasProperty() ?
        parameterName + "." + propertyName :
        parameterName;
    return BindingParameter.create("", newPropertyName, jdbcType);
  }

  public String getParameterName() {
    return parameterName;
  }

  public String getPropertyName() {
    return propertyName;
  }

  public JdbcType getJdbcType() {
    return jdbcType;
  }

  public String getFullName() {
    return Strings.getFullName(parameterName, propertyName);
  }

  public boolean hasProperty() {
    return Strings.isNotEmpty(propertyName);
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    final BindingParameter other = (BindingParameter) obj;
    return Objects.equal(this.getParameterName(), other.getParameterName())
        && Objects.equal(this.getPropertyName(), other.getPropertyName())
        && Objects.equal(this.getJdbcType(), other.getJdbcType());
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(getParameterName(), getPropertyName(), getJdbcType());
  }

  @Override
  public String toString() {
    return getFullName();
  }

}
