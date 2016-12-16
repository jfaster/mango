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

package org.jfaster.mango.mapper;

import org.jfaster.mango.mapper.tuple.Tuple3;
import org.jfaster.mango.mapper.tuple.Tuples;
import org.jfaster.mango.type.TypeHandler;
import org.jfaster.mango.type.TypeHandlerRegistry;
import org.jfaster.mango.util.jdbc.ResultSetWrapper;
import org.jfaster.mango.util.reflect.TypeToken;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author ash
 */
public class Tuple3RowMapper extends AbstractRowMapper<Tuple3> {

  private final static int LEN = 3;

  private final Class<?>[] classes = new Class<?>[LEN];

  public Tuple3RowMapper(Type tuple3Type) {
    if (!Tuple3.class.equals(TypeToken.of(tuple3Type).getRawType())) {
      throw new IllegalArgumentException("need type org.jfaster.mango.mapper.tuple.Tuple3, but " + tuple3Type);
    }
    TypeToken token = TypeToken.of(tuple3Type);
    for (int i = 0; i < LEN; i++) {
      classes[i] = token.resolveType(Tuple3.class.getTypeParameters()[i]).getRawType();
    }
  }

  @Override
  public Tuple3 mapRow(ResultSet rs, int rowNum) throws SQLException {
    ResultSetWrapper rsw = new ResultSetWrapper(rs);
    if (rsw.getColumnCount() != 3) {
      throw new MappingException("incorrect column count, expected 3 but " + rsw.getColumnCount());
    }

    Object[] values = new Object[LEN];
    for (int i = 0; i < classes.length; i++) {
      int index = i + 1;
      TypeHandler<?> typeHandler = TypeHandlerRegistry.getTypeHandler(classes[i], rsw.getJdbcType(index));
      Object value = typeHandler.getResult(rsw.getResultSet(), index);
      values[i] = value;
    }

    return Tuples.tuple(classes[0], classes[1], classes[2], values[0], values[1], values[2]);
  }

}
