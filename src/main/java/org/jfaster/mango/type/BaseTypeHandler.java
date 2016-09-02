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

import javax.annotation.Nullable;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author Clinton Begin
 * @author Simone Tripodi
 * @author ash
 */
public abstract class BaseTypeHandler<T> implements TypeHandler<T> {

  @Override
  public void setParameter(PreparedStatement ps, int index, @Nullable T parameter) throws SQLException {
    JdbcType jdbcType = getJdbcType();
    if (parameter == null) {
      try {
        ps.setNull(index, jdbcType.TYPE_CODE);
      } catch (SQLException e) {
        throw new TypeException("Error setting null for parameter #" + index + " with JdbcType " + jdbcType + ", " +
            "try setting a different JdbcType for this parameter", e);
      }
    } else {
      try {
        setNonNullParameter(ps, index, parameter, jdbcType);
      } catch (Exception e) {
        throw new TypeException("Error setting non null for parameter #" + index + " with JdbcType " + jdbcType + ", " +
            "try setting a different JdbcType for this parameter", e);
      }
    }
  }

  @Override
  public T getResult(ResultSet rs, int index) throws SQLException {
    T result;
    try {
      result = getNullableResult(rs, index);
    } catch (Exception e) {
      throw new TypeException("Error attempting to get column #" + index + " from result set", e);
    }
    if (rs.wasNull()) {
      return null;
    } else {
      return result;
    }
  }

  public abstract void setNonNullParameter(PreparedStatement ps, int index, T parameter, JdbcType jdbcType) throws SQLException;

  public abstract T getNullableResult(ResultSet rs, int index) throws SQLException;

  public abstract JdbcType getJdbcType();
}
