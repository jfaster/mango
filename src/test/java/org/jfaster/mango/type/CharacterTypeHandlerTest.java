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

package org.jfaster.mango.type;

import org.jfaster.mango.util.jdbc.JdbcType;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CharacterTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<Character> TYPE_HANDLER = new CharacterTypeHandler();

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, 'a');
    verify(ps).setString(1, "a");
  }

  @Test
  public void shouldSetNullParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, null);
    verify(ps).setNull(1, JdbcType.VARCHAR.TYPE_CODE);
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    when(rs.getString(1)).thenReturn("a");
    when(rs.wasNull()).thenReturn(false);
    assertEquals(new Character('a'), TYPE_HANDLER.getResult(rs, 1));
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getString(1)).thenReturn(null);
    when(rs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(rs, 1));
  }

}
