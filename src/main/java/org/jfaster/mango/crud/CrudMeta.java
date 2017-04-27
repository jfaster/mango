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

package org.jfaster.mango.crud;

import org.jfaster.mango.annotation.Column;
import org.jfaster.mango.annotation.ID;
import org.jfaster.mango.util.Strings;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author ash
 */
public class CrudMeta {

  private final List<String> properties;

  private final List<String> columns;

  private final String propertyId;

  private final String columnId;

  private final boolean isAutoGenerateId;

  public CrudMeta(Class<?> clazz) {
    List<String> props = new ArrayList<String>();
    List<String> cols = new ArrayList<String>();
    String propId = null;
    String colId = null;
    Field[] fields = clazz.getDeclaredFields();
    Boolean autoGenerateId = null;
    for (Field field : fields) {
      String prop = field.getName();
      if (!prop.startsWith("$")) { // 代码覆盖率工具Jacoco会为pojo对象织入$jacocoData字段
        Column colAnno = field.getAnnotation(Column.class);
        String col = colAnno != null ?
            colAnno.value() :
            Strings.underscoreName(prop);
        props.add(prop);
        cols.add(col);

        ID idAnno = field.getAnnotation(ID.class);
        if (idAnno != null) {
          if (propId != null || colId != null) {
            throw new IllegalStateException("duplicate ID annotation");
          }
          propId = prop;
          colId = col;
          autoGenerateId = idAnno.autoGenerateId();
        }
      }
    }
    if (autoGenerateId == null) {
      throw new IllegalStateException("need ID annotation on field to indicate primary key");
    }

    properties = Collections.unmodifiableList(props);
    columns = Collections.unmodifiableList(cols);
    propertyId = propId;
    columnId = colId;
    isAutoGenerateId = autoGenerateId;
  }

  public List<String> getProperties() {
    return properties;
  }

  public List<String> getColumns() {
    return columns;
  }

  public String getPropertyId() {
    return propertyId;
  }

  public String getColumnId() {
    return columnId;
  }

  public boolean isAutoGenerateId() {
    return isAutoGenerateId;
  }

}
