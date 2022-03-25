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

import org.jfaster.mango.annotation.*;
import org.jfaster.mango.util.ManipulateStringNames;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.bean.BeanUtil;
import org.jfaster.mango.util.bean.PropertyMeta;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author ash
 */
public class CrudMeta {

  private final List<String> properties;

  private final List<String> columns;

  private final Map<String, String> propertyToColumnMap;

  private final Map<String, Type> propertyToTypeMap;

  // 使用 @ID 修饰的类属性
  private final String property4Id;

  // 使用 @ID 修饰的类属性对应的数据库列
  private final String column4Id;

  // 使用 @AutoGenerated 修饰的类属性
  private final String property4AutoGenerated;

  // 使用 @AutoGenerated 修饰的类属性对应的数据库列
  private final String column4AutoGenerated;

  public CrudMeta(Class<?> clazz) {
    List<String> props = new ArrayList<String>();
    List<String> cols = new ArrayList<String>();
    Map<String, String> propToColMap = new HashMap<String, String>();
    HashMap<String, Type> propToTypeMap = new HashMap<String, Type>();
    String prop4Id = null;
    String col4Id = null;
    String prop4AutoGenerated = null;
    String col4AutoGenerated = null;
    for (PropertyMeta propertyMeta : BeanUtil.fetchPropertyMetas(clazz)) {
      Ignore igAnno = propertyMeta.getPropertyAnno(Ignore.class);
      if (igAnno != null) {
        continue;
      }
      String prop = propertyMeta.getName();
      Column colAnno = propertyMeta.getPropertyAnno(Column.class);
      String col = colAnno != null ?
          colAnno.value() :
          ManipulateStringNames.underscoreName(prop);
      props.add(prop);
      cols.add(col);
      propToColMap.put(prop, col);
      propToTypeMap.put(prop, propertyMeta.getType());

      AutoGeneratedID autoGeneratedIDAnno = propertyMeta.getPropertyAnno(AutoGeneratedID.class);

      ID idAnno = propertyMeta.getPropertyAnno(ID.class);
      if (idAnno != null || autoGeneratedIDAnno != null) {
        if (prop4Id != null || col4Id != null) {
          throw new IllegalStateException("duplicate @ID annotation");
        }
        prop4Id = prop;
        col4Id = col;
      }

      AutoGenerated autoGeneratedAnno = propertyMeta.getPropertyAnno(AutoGenerated.class);
      if (autoGeneratedAnno != null || autoGeneratedIDAnno != null) {
        if (prop4AutoGenerated != null || col4AutoGenerated != null) {
          throw new IllegalStateException("duplicate @AutoGenerated annotation");
        }
        prop4AutoGenerated = prop;
        col4AutoGenerated = col;
      }
    }
    properties = Collections.unmodifiableList(props);
    columns = Collections.unmodifiableList(cols);
    propertyToColumnMap = Collections.unmodifiableMap(propToColMap);
    propertyToTypeMap = Collections.unmodifiableMap(propToTypeMap);
    property4Id = prop4Id;
    column4Id = col4Id;
    property4AutoGenerated = prop4AutoGenerated;
    column4AutoGenerated = col4AutoGenerated;
  }

  public List<String> getProperties() {
    return properties;
  }

  public List<String> getColumns() {
    return columns;
  }

  @Nullable
  public String getColumnByProperty(String property) {
    return propertyToColumnMap.get(property);
  }

  @Nullable
  public Type getTypeByProperty(String property) {
    return propertyToTypeMap.get(property);
  }

  public String getProperty4Id() {
    return property4Id;
  }

  public String getColumn4Id() {
    return column4Id;
  }

  public String getProperty4AutoGenerated() {
    return property4AutoGenerated;
  }

  public String getColumn4AutoGenerated() {
    return column4AutoGenerated;
  }

}
