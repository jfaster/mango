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

import org.jfaster.mango.exception.jdbc.IncorrectResultSetColumnCountException;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * 单列RowMapper
 *
 * @author ash
 */
public class SingleColumnRowMapper<T> implements RowMapper<T> {

    private Class<T> mappedClass;

    public SingleColumnRowMapper(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
    }

    @SuppressWarnings("unchecked")
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        // 验证列数
        ResultSetMetaData rsmd = rs.getMetaData();
        int nrOfColumns = rsmd.getColumnCount();
        if (nrOfColumns != 1) {
            throw new IncorrectResultSetColumnCountException("incorrect column count, expected 1 but " + nrOfColumns);
        }

        Object result = getColumnValue(rs, 1, this.mappedClass);
        return (T) result;
    }

    @Override
    public Class<T> getMappedClass() {
        return mappedClass;
    }

    protected Object getColumnValue(ResultSet rs, int index, Class requiredType) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index, requiredType);
    }

}
