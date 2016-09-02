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

import java.io.ByteArrayInputStream;
import java.sql.Blob;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Clinton Begin
 * @author ash
 */
public class BlobByteObjectArrayTypeHandler extends BaseTypeHandler<Byte[]> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int i, Byte[] parameter, JdbcType jdbcType)
      throws SQLException {
    ByteArrayInputStream bis = new ByteArrayInputStream(ByteArrayUtils.convertToPrimitiveArray(parameter));
    ps.setBinaryStream(i, bis, parameter.length);
  }

  @Override
  public Byte[] getNullableResult(ResultSet rs, int columnIndex)
      throws SQLException {
    Blob blob = rs.getBlob(columnIndex);
    return getBytes(blob);
  }

  private Byte[] getBytes(Blob blob) throws SQLException {
    Byte[] returnValue = null;
    if (blob != null) {
      returnValue = ByteArrayUtils.convertToObjectArray(blob.getBytes(1, (int) blob.length()));
    }
    return returnValue;
  }

  @Override
  public JdbcType getJdbcType() {
    return JdbcType.LONGVARBINARY;
  }

}
