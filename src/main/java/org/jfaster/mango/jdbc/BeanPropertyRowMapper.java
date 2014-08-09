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

import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;
import org.jfaster.mango.util.reflect.BeanInfoCache;
import org.jfaster.mango.util.reflect.BeanUtil;
import org.jfaster.mango.util.reflect.Reflection;

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

    private Map<String, PropertyDescriptor> mappedFields;

    public BeanPropertyRowMapper(Class<T> mappedClass) {
        initialize(mappedClass);
    }

    protected void initialize(Class<T> mappedClass) {
        this.mappedClass = mappedClass;
        this.mappedFields = new HashMap<String, PropertyDescriptor>();
        List<PropertyDescriptor> pds = BeanInfoCache.getPropertyDescriptors(mappedClass);
        for (PropertyDescriptor pd : pds) {
            if (pd.getWriteMethod() != null) {
                this.mappedFields.put(pd.getName().toLowerCase(), pd);
                String underscoredName = underscoreName(pd.getName());
                if (!pd.getName().toLowerCase().equals(underscoredName)) {
                    this.mappedFields.put(underscoredName, pd);
                }
            }
        }
    }

    private String underscoreName(String name) {
        if (Strings.isNullOrEmpty(name)) {
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
        T mappedObject = Reflection.instantiate(this.mappedClass);

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        for (int index = 1; index <= columnCount; index++) {
            String column = JdbcUtils.lookupColumnName(rsmd, index);
            PropertyDescriptor pd = this.mappedFields.get(column.trim().toLowerCase());
            if (pd != null) {
                Object value = getColumnValue(rs, index, pd);
                if (logger.isDebugEnabled() && rowNumber == 0) {
                    logger.debug("Mapping column '" + column + "' to property '" + pd.getName() + "' of type " + pd.getPropertyType());
                }
                BeanUtil.setPropertyValue(mappedObject, pd.getName(), value);
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
