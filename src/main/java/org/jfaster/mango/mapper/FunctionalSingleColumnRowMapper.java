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

package org.jfaster.mango.mapper;

import org.jfaster.mango.exception.IncorrectResultSetColumnCountException;
import org.jfaster.mango.invoker.Function;
import org.jfaster.mango.jdbc.JdbcUtils;

import java.lang.reflect.Type;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;

/**
 * 函数式单列RowMapper
 *
 * @author ash
 */
public class FunctionalSingleColumnRowMapper<T> implements RowMapper<T> {

    private Function function;
    private Class<?> fromDbClass;
    private Class<T> mappedClass;
    private Type mappedType;

    public FunctionalSingleColumnRowMapper(Function function, Class<?> fromDbClass,
                                           Class<T> mappedClass, Type mappedType) {
        this.function = function;
        this.fromDbClass = fromDbClass;
        this.mappedClass = mappedClass;
        this.mappedType = mappedType;
    }

    @SuppressWarnings("unchecked")
    public T mapRow(ResultSet rs, int rowNum) throws SQLException {
        // 验证列数
        ResultSetMetaData rsmd = rs.getMetaData();
        int nrOfColumns = rsmd.getColumnCount();
        if (nrOfColumns != 1) {
            throw new IncorrectResultSetColumnCountException("incorrect column count, expected 1 but " + nrOfColumns);
        }

        Object fromDbValue = getColumnValue(rs, 1, this.fromDbClass);
        Object result = function.apply(fromDbValue, mappedType);
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
