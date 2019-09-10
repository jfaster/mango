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
import org.jfaster.mango.invoker.TransferableInvoker;
import org.jfaster.mango.type.TypeHandler;
import org.jfaster.mango.type.TypeHandlerRegistry;
import org.jfaster.mango.util.PropertyTokenizer;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.jdbc.ResultSetWrapper;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;
import org.jfaster.mango.util.reflect.Reflection;
import org.jfaster.mango.util.reflect.TypeToken;

import java.sql.ResultSet;
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

  private Map<String, TransferableInvoker> invokerMap;

  private Map<String, String> columnToPropertyMap;

  private boolean checkColumn;

  public BeanPropertyRowMapper(Class<T> mappedClass, Map<String, String> propertyToColumnMap, boolean checkColumn) {
    initialize(mappedClass, propertyToColumnMap, checkColumn);
  }

  void initialize(Class<T> mappedClass, Map<String, String> propertyToColumnMap, boolean checkColumn) {
    this.mappedClass = mappedClass;
    this.checkColumn = checkColumn;
    this.columnToPropertyMap = new HashMap<>();
    this.invokerMap = new HashMap<>();

    // 初始化columnToPropertyPathMap
    for (Map.Entry<String, String> entry : propertyToColumnMap.entrySet()) {
      String property = entry.getKey();
      PropertyTokenizer propToken = new PropertyTokenizer(property);
      if (propToken.hasNext()) {
        columnToPropertyMap.put(entry.getValue().toLowerCase(), property);
      }
    }

    // 初始化invokerMap
    List<TransferableInvoker> invokers = InvokerCache.getInvokers(mappedClass);
    for (TransferableInvoker invoker : invokers) {
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

    ResultSetWrapper rsw = new ResultSetWrapper(rs);
    int columnCount = rsw.getColumnCount();

    for (int index = 1; index <= columnCount; index++) {
      String columnName = rsw.getColumnName(index);
      String lowerCaseColumnName = columnName.toLowerCase();
      String propertyPath = columnToPropertyMap.get(lowerCaseColumnName);

      //TODO 优化
      if (propertyPath != null) { // 使用自定义多级映射
        setValueByProperty(mappedObject, propertyPath, rsw, index, rowNumber);
      } else {
        setValueByProperty(mappedObject, lowerCaseColumnName, rsw, index, rowNumber);
      }
    }
    return mappedObject;
  }
  private void setValueByProperty(Object mappedObject, String lowerCaseColumnName, ResultSetWrapper rsw,
                                  int index, int rowNumber) throws SQLException {

    TransferableInvoker invoker = invokerMap.get(lowerCaseColumnName);
    if (invoker != null) {
      Class<?> columnRawType = TypeToken.of(invoker.getColumnType()).getRawType();
      TypeHandler<?> typeHandler = TypeHandlerRegistry.getTypeHandler(columnRawType, rsw.getJdbcType(index));
      Object value = typeHandler.getResult(rsw.getResultSet(), index);
      if (logger.isDebugEnabled() && rowNumber == 0) {
        logger.debug("Mapping column '" + rsw.getColumnName(index) + "' to property '" +
            invoker.getName() + "' of type " + columnRawType);
      }
      invoker.invokeSet(mappedObject, value);
    } else {
      if (checkColumn) {
        throw new MappingException("Unable to map column '" + rsw.getColumnName(index) +
            "' to any property of '" + mappedClass + "'");
      }
    }
  }

  @Override
  public Class<T> getMappedClass() {
    return mappedClass;
  }

}
