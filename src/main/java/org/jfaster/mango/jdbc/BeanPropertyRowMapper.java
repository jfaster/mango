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

import org.jfaster.mango.invoker.FunctionalSetterInvokerGroup;
import org.jfaster.mango.invoker.InvokerCache;
import org.jfaster.mango.invoker.SetterInvoker;
import org.jfaster.mango.invoker.UnreachablePropertyException;
import org.jfaster.mango.reflect.Reflection;
import org.jfaster.mango.base.PropertyTokenizer;
import org.jfaster.mango.base.Strings;
import org.jfaster.mango.base.logging.InternalLogger;
import org.jfaster.mango.base.logging.InternalLoggerFactory;

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

    private Map<String, String> columnToPropertyMap;

    private boolean checkColumn;

    public BeanPropertyRowMapper(Class<T> mappedClass, Map<String, String> propertyToColumnMap, boolean checkColumn) {
        initialize(mappedClass, propertyToColumnMap, checkColumn);
    }

    protected void initialize(Class<T> mappedClass, Map<String, String> propertyToColumnMap, boolean checkColumn) {
        this.mappedClass = mappedClass;
        this.checkColumn = checkColumn;
        this.columnToPropertyMap = new HashMap<String, String>();
        this.invokerMap = new HashMap<String, SetterInvoker>();

        // 初始化columnToPropertyPathMap
        for (Map.Entry<String, String> entry : propertyToColumnMap.entrySet()) {
            String property = entry.getKey();
            PropertyTokenizer propToken = new PropertyTokenizer(property);
            if (propToken.hasNext()) {
                columnToPropertyMap.put(entry.getValue().toLowerCase(), property);
            }
        }

        // 初始化invokerMap
        List<SetterInvoker> invokers = InvokerCache.getSetterInvokers(mappedClass);
        for (SetterInvoker invoker : invokers) {
            String column = propertyToColumnMap.get(invoker.getName());
            if (column != null) { // 使用配置映射
                invokerMap.put(column.toLowerCase(), invoker);
            } else { // 使用约定映射
                invokerMap.put(invoker.getName().toLowerCase(), invoker);
                String underscoredName = Strings.underscoreName(invoker.getName());
                if (!invoker.getName().toLowerCase().equals(underscoredName)) {
                    invokerMap.put(underscoredName, invoker);
                }
            }
        }
    }

    public T mapRow(ResultSet rs, int rowNumber) throws SQLException {
        T mappedObject = Reflection.instantiate(mappedClass);

        ResultSetMetaData rsmd = rs.getMetaData();
        int columnCount = rsmd.getColumnCount();

        for (int index = 1; index <= columnCount; index++) {
            String originalColumn = JdbcUtils.lookupColumnName(rsmd, index).trim();
            String column = originalColumn.toLowerCase();
            String propertyPath = columnToPropertyMap.get(column);
            if (propertyPath != null) { // 使用自定义多级映射
                setValueByPropertyPath(mappedObject, propertyPath, rs, index, rowNumber, originalColumn);
            } else {
                PropertyTokenizer prop = new PropertyTokenizer(originalColumn);
                if (prop.hasNext()) { // select语句中的字段存在多级映射
                    setValueByPropertyPath(mappedObject, originalColumn, rs, index, rowNumber, originalColumn);
                } else { // 单级映射（包含约定和自定义）
                    setValueByProperty(mappedObject, column, rs, index, rowNumber, originalColumn);
                }
            }
        }
        return mappedObject;
    }

    private void setValueByPropertyPath(Object mappedObject, String propertyPath, ResultSet rs,
                                    int index, int rowNumber, String originalColumn) throws SQLException {

        FunctionalSetterInvokerGroup g = null;
        try {
            g = FunctionalSetterInvokerGroup.create(mappedClass, propertyPath);
        } catch (UnreachablePropertyException e) {
            if (checkColumn) {
                throw new MappingException("Unable to map column '" + originalColumn +
                        "' to property '" + propertyPath + "'; Cause: " + e.getMessage());
            }
        }
        if (g != null) {
            Object value = JdbcUtils.getResultSetValue(rs, index, g.getTargetType());
            if (logger.isDebugEnabled() && rowNumber == 0) {
                logger.debug("Mapping column '" + originalColumn + "' to property '" +
                        propertyPath + "' of type " + g.getTargetType());
            }
            g.invoke(mappedObject, value);
        }
    }

    private void setValueByProperty(Object mappedObject, String column, ResultSet rs,
                                    int index, int rowNumber, String originalColumn) throws SQLException {

        SetterInvoker invoker = invokerMap.get(column);
        if (invoker != null) {
            Object value = JdbcUtils.getResultSetValue(rs, index, invoker.getParameterRawType());
            if (logger.isDebugEnabled() && rowNumber == 0) {
                logger.debug("Mapping column '" + originalColumn + "' to property '" +
                        invoker.getName() + "' of type " + invoker.getParameterRawType());
            }
            invoker.invoke(mappedObject, value);
        } else {
            if (checkColumn) {
                throw new MappingException("Unable to map column '" + originalColumn +
                        "' to any property of '" + mappedClass + "'");
            }
        }
    }

    @Override
    public Class<T> getMappedClass() {
        return mappedClass;
    }

}
