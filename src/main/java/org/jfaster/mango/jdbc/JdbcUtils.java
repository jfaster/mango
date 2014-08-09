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

import org.jfaster.mango.exception.CannotGetJdbcConnectionException;
import org.jfaster.mango.exception.UnreachableCodeException;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import javax.sql.DataSource;
import java.sql.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author ash
 */
public class JdbcUtils {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(JdbcUtils.class);


    /**
     * 从数据源中获得一个连接
     *
     * @param ds
     * @return
     */
    public static Connection getConnection(DataSource ds) {
        try {
            return ds.getConnection();
        } catch (SQLException e) {
            throw new CannotGetJdbcConnectionException("Could not get JDBC Connection", e);
        }
    }

    /**
     * 关闭连接
     *
     * @param conn
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException e) {
                logger.error("Could not close JDBC Connection", e);
            } catch (Throwable e) {
                logger.error("Unexpected exception on closing JDBC Connection", e);
            }
        }
    }

    /**
     * 关闭语句
     *
     * @param stmt
     */
    public static void closeStatement(Statement stmt) {
        if (stmt != null) {
            try {
                stmt.close();
                stmt = null;
            } catch (SQLException e) {
                logger.error("Could not close JDBC Statement", e);
            } catch (Throwable e) {
                logger.error("Unexpected exception on closing JDBC Statement", e);
            }
        }
    }

    /**
     * 关闭结果集
     *
     * @param rs
     */
    public static void closeResultSet(ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
                rs = null;
            } catch (SQLException e) {
                logger.error("Could not close JDBC ResultSet", e);
            } catch (Throwable e) {
                logger.error("Unexpected exception on closing JDBC ResultSet", e);
            }
        }
    }

    public static Object getResultSetValue(ResultSet rs, int index, Class requiredType) throws SQLException {

        Object value = null;
        boolean wasNullCheck = false;

        // Explicitly extract typed value, as far as possible.
        if (String.class.equals(requiredType)) {
            value = rs.getString(index);
        } else if (boolean.class.equals(requiredType) || Boolean.class.equals(requiredType)) {
            value = rs.getBoolean(index);
            wasNullCheck = true;
        } else if (byte.class.equals(requiredType) || Byte.class.equals(requiredType)) {
            value = rs.getByte(index);
            wasNullCheck = true;
        } else if (short.class.equals(requiredType) || Short.class.equals(requiredType)) {
            value = rs.getShort(index);
            wasNullCheck = true;
        } else if (int.class.equals(requiredType) || Integer.class.equals(requiredType)) {
            value = rs.getInt(index);
            wasNullCheck = true;
        } else if (long.class.equals(requiredType) || Long.class.equals(requiredType)) {
            value = rs.getLong(index);
            wasNullCheck = true;
        } else if (float.class.equals(requiredType) || Float.class.equals(requiredType)) {
            value = rs.getFloat(index);
            wasNullCheck = true;
        } else if (double.class.equals(requiredType) || Double.class.equals(requiredType) ||
                Number.class.equals(requiredType)) {
            value = rs.getDouble(index);
            wasNullCheck = true;
        } else if (byte[].class.equals(requiredType)) {
            value = rs.getBytes(index);
        } else if (java.sql.Date.class.equals(requiredType)) {
            value = rs.getDate(index);
        } else if (java.sql.Time.class.equals(requiredType)) {
            value = rs.getTime(index);
        } else if (java.sql.Timestamp.class.equals(requiredType) || java.util.Date.class.equals(requiredType)) {
            value = rs.getTimestamp(index);
        } else if (java.math.BigDecimal.class.equals(requiredType)) {
            value = rs.getBigDecimal(index);
        } else if (java.sql.Blob.class.equals(requiredType)) {
            value = rs.getBlob(index);
        } else if (java.sql.Clob.class.equals(requiredType)) {
            value = rs.getClob(index);
        } else {
            throw new UnreachableCodeException();
        }

        // Perform was-null check if demanded (for results that the
        // JDBC driver returns as primitives).
        if (wasNullCheck && value != null && rs.wasNull()) {
            value = null;
        }
        return value;
    }

    public static void setParameterValue(PreparedStatement ps, int index, Object value) throws SQLException {
        if (value != null && java.util.Date.class.equals(value.getClass())) {
            ps.setTimestamp(index, new Timestamp(((java.util.Date) value).getTime()));
        } else {
            ps.setObject(index, value);
        }
    }

    private final static Set<Class<?>> singleColumClassSet = new HashSet<Class<?>>();
    static {
        // 字符串
        singleColumClassSet.add(String.class);

        // 特殊类型
        singleColumClassSet.add(java.math.BigDecimal.class);
        singleColumClassSet.add(java.util.Date.class);

        // jdbc中的类型
        singleColumClassSet.add(byte[].class);
        singleColumClassSet.add(java.sql.Date.class);
        singleColumClassSet.add(java.sql.Time.class);
        singleColumClassSet.add(java.sql.Timestamp.class);
        singleColumClassSet.add(java.sql.Blob.class);
        singleColumClassSet.add(java.sql.Clob.class);

        // 基本数据类型
        singleColumClassSet.add(boolean.class);
        singleColumClassSet.add(byte.class);
        singleColumClassSet.add(char.class);
        singleColumClassSet.add(double.class);
        singleColumClassSet.add(float.class);
        singleColumClassSet.add(int.class);
        singleColumClassSet.add(long.class);
        singleColumClassSet.add(short.class);
        singleColumClassSet.add(Boolean.class);
        singleColumClassSet.add(Byte.class);
        singleColumClassSet.add(Character.class);
        singleColumClassSet.add(Double.class);
        singleColumClassSet.add(Float.class);
        singleColumClassSet.add(Integer.class);
        singleColumClassSet.add(Long.class);
        singleColumClassSet.add(Short.class);
    }

    /**
     * 返回是否是单列类型
     *
     * @param clazz
     * @return
     */
    public static boolean isSingleColumnClass(Class clazz) {
        return singleColumClassSet.contains(clazz);
    }


    /**
     * 返回列序号对应的列名字
     *
     * @param resultSetMetaData
     * @param columnIndex
     * @return
     * @throws java.sql.SQLException
     */
    public static String lookupColumnName(ResultSetMetaData resultSetMetaData, int columnIndex) throws SQLException {
        String name = resultSetMetaData.getColumnLabel(columnIndex);
        if (name == null || name.length() < 1) {
            name = resultSetMetaData.getColumnName(columnIndex);
        }
        return name;
    }

}
