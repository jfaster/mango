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

package org.jfaster.mango.jdbc;

import org.jfaster.mango.mapper.RowMapper;
import org.jfaster.mango.util.Arrays;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class ArrayResultSetExtractor<T> implements ResultSetExtractor<Object> {

  private final RowMapper<T> rowMapper;

  public ArrayResultSetExtractor(RowMapper<T> rowMapper) {
    this.rowMapper = rowMapper;
  }

  @Override
  public Object extractData(ResultSet rs) throws SQLException {
    List<T> list = new ArrayList<T>();
    int rowNum = 0;
    while (rs.next()) {
      list.add(rowMapper.mapRow(rs, rowNum++));
    }
    return Arrays.toArray(list, rowMapper.getMappedClass());
  }

}
