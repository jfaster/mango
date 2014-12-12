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

import org.jfaster.mango.jdbc.GeneratedKeyHolder;
import org.jfaster.mango.jdbc.JdbcOperations;
import org.jfaster.mango.mapper.RowMapper;

import javax.sql.DataSource;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class JdbcOperationsAdapter implements JdbcOperations {

    @Override
    public <T> T queryForObject(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> List<T> queryForList(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Set<T> queryForSet(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> Object queryForArray(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(DataSource ds, String sql, Object[] args) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int update(DataSource ds, String sql, Object[] args, GeneratedKeyHolder holder) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] batchUpdate(DataSource ds, String sql, List<Object[]> batchArgs) {
        throw new UnsupportedOperationException();
    }

    @Override
    public int[] batchUpdate(DataSource ds, List<String> sqls, List<Object[]> batchArgs) {
        throw new UnsupportedOperationException();
    }

}
