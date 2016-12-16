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
import org.jfaster.mango.support.ResultSetMetaDataAapter;
import org.jfaster.mango.util.reflect.TypeToken;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.when;

/**
 * @author ash
 */
@RunWith(MockitoJUnitRunner.class)
public class Tuple3RowMapperTest {

  @Mock
  protected ResultSet rs;

  @SuppressWarnings("unchecked")
  @Test
  public void testMapRow() throws Exception {
    when(rs.getMetaData()).thenReturn(new ResultSetMetaDataAapter() {
      @Override
      public int getColumnCount() throws SQLException {
        return 3;
      }
    });
    when(rs.getInt(1)).thenReturn(29);
    when(rs.getString(2)).thenReturn("ash");
    when(rs.getDouble(3)).thenReturn(100.0);

    Tuple3RowMapper mapper = new Tuple3RowMapper(new TypeToken<Tuple3<Integer, String, Double>>() {}.getType());
    Tuple3<Integer, String, Double> tuple3 = mapper.mapRow(rs, 0);

    assertThat(tuple3.value1(), equalTo(29));
    assertThat(tuple3.value2(), equalTo("ash"));
    assertThat(tuple3.value3(), equalTo(100.0));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMapRowNull() throws Exception {
    when(rs.getMetaData()).thenReturn(new ResultSetMetaDataAapter() {
      @Override
      public int getColumnCount() throws SQLException {
        return 3;
      }
    });
    when(rs.getInt(1)).thenReturn(0);
    when(rs.getString(2)).thenReturn(null);
    when(rs.getDouble(3)).thenReturn(0.0);
    when(rs.wasNull()).thenReturn(true);

    Tuple3RowMapper mapper = new Tuple3RowMapper(new TypeToken<Tuple3<Integer, String, Double>>() {}.getType());
    Tuple3<Integer, String, Double> tuple3 = mapper.mapRow(rs, 0);

    assertThat(tuple3.value1(), equalTo(null));
    assertThat(tuple3.value2(), equalTo(null));
    assertThat(tuple3.value3(), equalTo(null));
  }

}








