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

import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Paul Krause
 * @author ash
 */
public class BigIntegerTypeHandler extends BaseTypeHandler<BigInteger> {

  @Override
  public void setNonNullParameter(PreparedStatement ps, int index, BigInteger parameter, JdbcType jdbcType) throws SQLException {
    ps.setBigDecimal(index, new BigDecimal(parameter));
  }

  @Override
  public BigInteger getNullableResult(ResultSet rs, int index) throws SQLException {
    BigDecimal bigDecimal = rs.getBigDecimal(index);
    return bigDecimal == null ? null : bigDecimal.toBigInteger();
  }

  @Override
  public JdbcType getJdbcType() {
    return JdbcType.DECIMAL;
  }

}
