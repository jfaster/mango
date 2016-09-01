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

import org.jfaster.mango.mapper.AbstractRowMapper;
import org.jfaster.mango.mapper.RowMapper;
import org.junit.Test;

import java.sql.ResultSet;
import java.sql.SQLException;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class AbstractRowMapperTest {

  @Test
  public void testInt() throws Exception {
    class IntegerRowMapper extends AbstractRowMapper<Integer> {
      @Override
      public Integer mapRow(ResultSet rs, int rowNum) throws SQLException {
        return rs.getInt(1);
      }
    }
    RowMapper mapper = new IntegerRowMapper();
    Class<?> mappedClass = mapper.getMappedClass();
    assertThat(mappedClass.equals(Integer.class), is(true));
  }

}
