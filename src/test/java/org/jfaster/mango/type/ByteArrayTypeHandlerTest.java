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

import static org.junit.Assert.assertArrayEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ByteArrayTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<byte[]> TYPE_HANDLER = new ByteArrayTypeHandler();

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    TYPE_HANDLER.setParameter(ps, 1, new byte[] { 1, 2, 3 });
    verify(ps).setBytes(1, new byte[] { 1, 2, 3 });
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    when(rs.getBytes(1)).thenReturn(new byte[] { 1, 2, 3 });
    when(rs.wasNull()).thenReturn(false);
    assertArrayEquals(new byte[] { 1, 2, 3 }, TYPE_HANDLER.getResult(rs, 1));
  }

  @Override
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    // Unnecessary
  }

}