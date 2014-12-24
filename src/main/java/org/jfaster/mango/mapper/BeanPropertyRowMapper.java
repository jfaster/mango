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

import org.jfaster.mango.invoker.InvokerCache;
import org.jfaster.mango.invoker.SetterInvoker;
import org.jfaster.mango.jdbc.JdbcUtils;
import org.jfaster.mango.reflect.Reflection;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 单列或多列组装对象RowMapper
 *
 * @author ash
 */
public class BeanPropertyRowMapper<T> implements RowMapper<T> {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(BeanPropertyRowMapper.class);

    private Class<T> mappedClass;

    private Map<String, SetterInvoker> invokerMap;

    public BeanPropertyRowMapper(Class<T> mappedClass, Map<String, String> propertyToColumnMap) {
        initialize(mappedClass, propertyToColumnMap);
    }

    protected void initialize(Class<T> mappedClass, Map<String, String> propertyToColumnMap) {
        this.mappedClass = mappedClass;
        this.invokerMap = new HashMap<String, SetterInvoker>();
        List<SetterInvoker> invokers = InvokerCache.getSetterInvokers(mappedClass);
        for (SetterInvoker invoker : invokers) {
            String column = propertyToColumnMap.get(invoker.getName().toLowerCase());
            if (column != null) {
                invokerMap.put(column, invoker);
            } else {
                invokerMap.put(invoker.getName().toLowerCase(), invoker);
                String underscoredName = underscoreName(invoker.getName());
                if (!invoker.getName().toLowerCase().equals(underscoredName)) {
                    invokerMap.put(underscoredName, invoker);
                }
            }
        }
    }

    private String underscoreName(String name) {
        if (Strings.isEmpty(name)) {
            return "";
        }
        StringBuilder result = new StringBuilder();
        result.append(name.substring(0, 1).toLowerCase());
        for (int i = 1; i < name.length(); i++) {
            String s = name.substring(i, i + 1);
            String slc = s.toLowerCase();
            if (!s.equals(slc)) {
                result.append("_").append(slc);
            } else {
                result.append(s);
            }
        }
        return result.toString();
    }

    public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
        T mappedObject = Reflection.instantiate(mappedClass);

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        for (int index = 1; index <= columnCount; index++) {
            String column = JdbcUtils.lookupColumnName(rsmd, index);
            SetterInvoker invoker = invokerMap.get(column.trim().toLowerCase());
            if (invoker != null) {
                Object value = JdbcUtils.getResultSetValue(rs, index, invoker.getPropertyRawType());
                if (logger.isDebugEnabled() && rowNumber == 0) {
                    logger.debug("Mapping column '" + column + "' to property '" +
                            invoker.getName() + "' of type " + invoker.getPropertyRawType());
                }
                invoker.invoke(mappedObject, value);
            }
        }
        return mappedObject;
    }

    @Override
    public Class<T> getMappedClass() {
        return mappedClass;
    }

}
