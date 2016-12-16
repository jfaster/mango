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

import org.jfaster.mango.mapper.tuple.Tuple2;
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
public class Tuple2RowMapperTest {

  @Mock
  protected ResultSet rs;

  @SuppressWarnings("unchecked")
  @Test
  public void testMapRow() throws Exception {
    when(rs.getMetaData()).thenReturn(new ResultSetMetaDataAapter() {
      @Override
      public int getColumnCount() throws SQLException {
        return 2;
      }
    });
    when(rs.getInt(1)).thenReturn(29);
    when(rs.getString(2)).thenReturn("ash");

    Tuple2RowMapper mapper = new Tuple2RowMapper(new TypeToken<Tuple2<Integer, String>>() {}.getType());
    Tuple2<Integer, String> tuple2 = mapper.mapRow(rs, 0);

    assertThat(tuple2.value1(), equalTo(29));
    assertThat(tuple2.value2(), equalTo("ash"));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testMapRowNull() throws Exception {
    when(rs.getMetaData()).thenReturn(new ResultSetMetaDataAapter() {
      @Override
      public int getColumnCount() throws SQLException {
        return 2;
      }
    });
    when(rs.getInt(1)).thenReturn(29);
    when(rs.getString(2)).thenReturn(null);

    Tuple2RowMapper mapper = new Tuple2RowMapper(new TypeToken<Tuple2<Integer, String>>() {}.getType());
    Tuple2<Integer, String> tuple2 = mapper.mapRow(rs, 0);

    assertThat(tuple2.value1(), equalTo(29));
    assertThat(tuple2.value2(), equalTo(null));
  }

}








