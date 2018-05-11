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

package org.jfaster.mango.crud.common.builder;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class CommonUpdateBuilder extends AbstractCommonBuilder {

  private final static String SQL_TEMPLATE = "update #table set %s where %s";

  private final String propertyId;

  private final String columnId;

  private final List<String> properties ;

  private final List<String> columns ;

  public CommonUpdateBuilder(String propId, List<String> props,
                             List<String> cols) {
    int index = props.indexOf(propId);
    if (index < 0) {
      throw new IllegalArgumentException("error property id [" + propId + "]");
    }
    propertyId = propId;
    properties = new ArrayList<String>(props);
    columns = new ArrayList<String>(cols);
    columnId = columns.remove(index);
    properties.remove(index);
  }

  @Override
  public String buildSql() {
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < properties.size(); i++) {
      String prop = ":" + properties.get(i);
      String col = columns.get(i);
      sb.append(String.format("#if (%s != null) %s = %s,#end ", prop, col, prop));
    }
    sb.append("#trim_comma");
    String str = columnId + " = :" + propertyId;
    return String.format(SQL_TEMPLATE, sb.toString(), str);
  }

}
