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

import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;

import java.io.Reader;
import java.sql.Clob;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NClobTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<String> TYPE_HANDLER = new NClobTypeHandler();

  @Mock
  protected Clob clob;

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, "Hello");
    verify(ps).setCharacterStream(Mockito.eq(1), Mockito.any(Reader.class), Mockito.eq(5));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    when(rs.getClob(1)).thenReturn(clob);
    when(rs.wasNull()).thenReturn(false);
    when(clob.length()).thenReturn(3l);
    when(clob.getSubString(1, 3)).thenReturn("Hello");
    assertEquals("Hello", TYPE_HANDLER.getResult(rs, 1));
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getClob(1)).thenReturn(null);
    when(rs.wasNull()).thenReturn(true);
    assertNull(TYPE_HANDLER.getResult(rs, 1));
  }

}