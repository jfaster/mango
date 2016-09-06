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

package org.jfaster.mango.support;

import org.jfaster.mango.binding.BoundSql;
import org.jfaster.mango.jdbc.*;
import org.jfaster.mango.jdbc.ListSupplier;
import org.jfaster.mango.jdbc.exception.DataAccessException;
import org.jfaster.mango.mapper.RowMapper;
import org.jfaster.mango.jdbc.SetSupplier;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class JdbcOperationsAdapter implements JdbcOperations {


  @Override
  public <T> T queryForObject(DataSource ds, BoundSql boundSql, RowMapper<T> rowMapper) throws DataAccessException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> List<T> queryForList(DataSource ds, BoundSql boundSql, ListSupplier listSupplier, RowMapper<T> rowMapper) throws DataAccessException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> Set<T> queryForSet(DataSource ds, BoundSql boundSql, SetSupplier setSupplier, RowMapper<T> rowMapper) throws DataAccessException {
    throw new UnsupportedOperationException();
  }

  @Override
  public <T> Object queryForArray(DataSource ds, BoundSql boundSql, RowMapper<T> rowMapper) throws DataAccessException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int update(DataSource ds, BoundSql boundSql) throws DataAccessException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int update(DataSource ds, BoundSql boundSql, GeneratedKeyHolder holder) throws DataAccessException {
    throw new UnsupportedOperationException();
  }

  @Override
  public int[] batchUpdate(DataSource ds, List<BoundSql> boundSqls) throws DataAccessException {
    throw new UnsupportedOperationException();
  }

}
