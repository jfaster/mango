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

package org.jfaster.mango.util.jdbc;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class ResultSetWrapper {

  private final ResultSet resultSet;

  private final List<String> columnNames = new ArrayList<String>();
  private final List<JdbcType> jdbcTypes = new ArrayList<JdbcType>();

  public ResultSetWrapper(ResultSet rs) throws SQLException {
    this.resultSet = rs;
    final ResultSetMetaData metaData = rs.getMetaData();
    final int columnCount = metaData.getColumnCount();
    for (int index = 1; index <= columnCount; index++) {
      String columnName = metaData.getColumnLabel(index);
      if (columnName == null || columnName.length() < 1) {
        columnName = metaData.getColumnName(index);
      }
      columnNames.add(columnName.trim());
      jdbcTypes.add(JdbcType.forCode(metaData.getColumnType(index)));
    }
  }

  public int getColumnCount() {
    return columnNames.size();
  }

  public String getColumnName(int index) {
    return columnNames.get(index - 1);
  }

  public JdbcType getJdbcType(int index) {
    return jdbcTypes.get(index - 1);
  }

  public ResultSet getResultSet() {
    return resultSet;
  }

}
