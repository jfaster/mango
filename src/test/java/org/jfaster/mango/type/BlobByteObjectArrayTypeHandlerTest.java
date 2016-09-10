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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;

import java.io.ByteArrayInputStream;
import java.sql.Blob;

import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.nullValue;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

public class BlobByteObjectArrayTypeHandlerTest extends BaseTypeHandlerTest {

  private static final TypeHandler<Byte[]> TYPE_HANDLER = new BlobByteObjectArrayTypeHandler();

  @Mock
  protected Blob blob;

  @Override
  @Test
  public void shouldSetParameter() throws Exception {
    final ArgumentCaptor<Integer> positionCaptor = ArgumentCaptor.forClass(Integer.class);
    final ArgumentCaptor<ByteArrayInputStream> byteArrayCaptor = ArgumentCaptor.forClass(ByteArrayInputStream.class);
    final ArgumentCaptor<Integer> lengthCaptor = ArgumentCaptor.forClass(Integer.class);
    doNothing().when(ps).setBinaryStream(positionCaptor.capture(), byteArrayCaptor.capture(), lengthCaptor.capture());
    TYPE_HANDLER.setParameter(ps, 1, new Byte[]{1, 2});
    ByteArrayInputStream actualIn = byteArrayCaptor.getValue();
    assertThat(positionCaptor.getValue(), is(1));
    assertThat(actualIn.read(), is(1));
    assertThat(actualIn.read(), is(2));
    assertThat(actualIn.read(), is(-1));
    assertThat(lengthCaptor.getValue(), is(2));
  }

  @Override
  @Test
  public void shouldGetResultFromResultSetByPosition() throws Exception {
    byte[] byteArray = new byte[]{1, 2};
    when(rs.getBlob(1)).thenReturn(blob);
    when(rs.wasNull()).thenReturn(false);
    when(blob.length()).thenReturn((long)byteArray.length);
    when(blob.getBytes(1, 2)).thenReturn(byteArray);
    assertThat(TYPE_HANDLER.getResult(rs, 1), is(new Byte[]{1, 2}));
  }

  @Override
  @Test
  public void shouldGetResultNullFromResultSetByPosition() throws Exception {
    when(rs.getBlob(1)).thenReturn(null);
    when(rs.wasNull()).thenReturn(true);
    assertThat(TYPE_HANDLER.getResult(rs, 1), nullValue());
  }

}