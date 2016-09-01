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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author ash
 */
public class ListResultSetExtractor<T> implements ResultSetExtractor<List<T>> {

  private final ListSupplier listSupplier;
  private final RowMapper<T> rowMapper;

  public ListResultSetExtractor(ListSupplier listSupplier, RowMapper<T> rowMapper) {
    this.listSupplier = listSupplier;
    this.rowMapper = rowMapper;
  }

  @Override
  public List<T> extractData(ResultSet rs) throws SQLException {
    List<T> results = listSupplier.get(rowMapper.getMappedClass());
    int rowNum = 0;
    while (rs.next()) {
      results.add(rowMapper.mapRow(rs, rowNum++));
    }
    return results;
  }

}
