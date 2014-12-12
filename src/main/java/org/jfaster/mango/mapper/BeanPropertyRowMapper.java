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

import org.jfaster.mango.jdbc.JdbcUtils;
import org.jfaster.mango.reflect.Beans;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;
import org.jfaster.mango.reflect.BeanInfoCache;
import org.jfaster.mango.reflect.Reflection;

import java.beans.PropertyDescriptor;
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

    private Map<String, PropertyDescriptor> mappedPropertis;

    public BeanPropertyRowMapper(Class<T> mappedClass, Map<String, String> propertyToColumnMap) {
        initialize(mappedClass, propertyToColumnMap);
    }

    protected void initialize(Class<T> mappedClass, Map<String, String> propertyToColumnMap) {
        this.mappedClass = mappedClass;
        this.mappedPropertis = new HashMap<String, PropertyDescriptor>();
        List<PropertyDescriptor> pds = BeanInfoCache.getPropertyDescriptors(mappedClass);
        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null) {
                String column = propertyToColumnMap.get(pd.getName().toLowerCase());
                if (column != null) {
                    mappedPropertis.put(column, pd);
                } else {
                    mappedPropertis.put(pd.getName().toLowerCase(), pd);
                    String underscoredName = underscoreName(pd.getName());
                    if (!pd.getName().toLowerCase().equals(underscoredName)) {
                        mappedPropertis.put(underscoredName, pd);
                    }
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
            PropertyDescriptor pd = mappedPropertis.get(column.trim().toLowerCase());
            if (pd != null) {
                Object value = getColumnValue(rs, index, pd);
                if (logger.isDebugEnabled() && rowNumber == 0) {
                    logger.debug("Mapping column '" + column + "' to property '" + pd.getName() + "' of type " + pd.getPropertyType());
                }
                Beans.setPropertyValue(mappedObject, pd.getName(), value);
            }
        }
        return mappedObject;
    }

    @Override
    public Class<T> getMappedClass() {
        return mappedClass;
    }


    protected Object getColumnValue(ResultSet rs, int index, PropertyDescriptor pd) throws SQLException {
        return JdbcUtils.getResultSetValue(rs, index, pd.getPropertyType());
    }

}
