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

package org.jfaster.mango.type;

import org.jfaster.mango.util.jdbc.JdbcType;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Clinton Begin
 * @author ash
 */
public final class TypeHandlerRegistry {

  private static final Map<Type, Map<JdbcType, TypeHandler<?>>> TYPE_HANDLER_MAP = new HashMap<Type, Map<JdbcType, TypeHandler<?>>>();
  static {
    register(Boolean.class, new BooleanTypeHandler());
    register(Byte.class, new ByteTypeHandler());
    register(Short.class, new ShortTypeHandler());
    register(Integer.class, new IntegerTypeHandler());
    register(Long.class, new LongTypeHandler());
    register(Float.class, new FloatTypeHandler());
    register(Double.class, new DoubleTypeHandler());
    register(Character.class, new CharacterTypeHandler());

    register(boolean.class, new BooleanTypeHandler());
    register(byte.class, new ByteTypeHandler());
    register(short.class, new ShortTypeHandler());
    register(int.class, new IntegerTypeHandler());
    register(long.class, new LongTypeHandler());
    register(float.class, new FloatTypeHandler());
    register(double.class, new DoubleTypeHandler());
    register(char.class, new CharacterTypeHandler());

    register(String.class, new StringTypeHandler());
    register(String.class, JdbcType.CHAR, new StringTypeHandler());
    register(String.class, JdbcType.CLOB, new ClobTypeHandler());
    register(String.class, JdbcType.VARCHAR, new StringTypeHandler());
    register(String.class, JdbcType.LONGVARCHAR, new ClobTypeHandler());
    register(String.class, JdbcType.NVARCHAR, new NStringTypeHandler());
    register(String.class, JdbcType.NCHAR, new NStringTypeHandler());
    register(String.class, JdbcType.NCLOB, new NClobTypeHandler());

    register(BigInteger.class, new BigIntegerTypeHandler());
    register(BigDecimal.class, new BigDecimalTypeHandler());

    register(Byte[].class, new ByteObjectArrayTypeHandler());
    register(Byte[].class, JdbcType.BLOB, new BlobByteObjectArrayTypeHandler());
    register(Byte[].class, JdbcType.LONGVARBINARY, new BlobByteObjectArrayTypeHandler());
    register(byte[].class, new ByteArrayTypeHandler());
    register(byte[].class, JdbcType.BLOB, new BlobTypeHandler());
    register(byte[].class, JdbcType.LONGVARBINARY, new BlobTypeHandler());

    register(Date.class, new DateTypeHandler());
    register(Date.class, JdbcType.DATE, new DateOnlyTypeHandler());
    register(Date.class, JdbcType.TIME, new TimeOnlyTypeHandler());

    register(java.sql.Date.class, new SqlDateTypeHandler());
    register(java.sql.Time.class, new SqlTimeTypeHandler());
    register(java.sql.Timestamp.class, new SqlTimestampTypeHandler());
  }

  public static boolean hasTypeHandler(Class<?> javaType) {
    return getNullableTypeHandler(javaType) != null;
  }

  @Nullable
  public static <T> TypeHandler<T> getNullableTypeHandler(Class<T> type) {
    return getNullableTypeHandler((Type) type, null);
  }

  public static <T> TypeHandler<T> getTypeHandler(Class<T> type) {
    return getTypeHandler(type, null);
  }

  @Nullable
  public static <T> TypeHandler<T> getNullableTypeHandler(Class<T> type, JdbcType jdbcType) {
    return getNullableTypeHandler((Type) type, jdbcType);
  }

  public static <T> TypeHandler<T> getTypeHandler(Class<T> type, JdbcType jdbcType) {
    TypeHandler<T> typeHandler = getNullableTypeHandler((Type) type, jdbcType);
    if (typeHandler == null) {
      throw new TypeException("Can't get type handle, java type is '" + type + "', jdbc type is '" + jdbcType + "'");
    }
    return typeHandler;
  }

  @SuppressWarnings("unchecked")
  @Nullable
  private static <T> TypeHandler<T> getNullableTypeHandler(Type type, JdbcType jdbcType) {
    Map<JdbcType, TypeHandler<?>> jdbcHandlerMap = TYPE_HANDLER_MAP.get(type);
    TypeHandler<?> handler = null;
    if (jdbcHandlerMap != null) {
      handler = jdbcHandlerMap.get(jdbcType);
      if (handler == null) {
        handler = jdbcHandlerMap.get(null);
      }
    }
    return (TypeHandler<T>) handler;
  }

  public static <T> void register(Class<T> javaType, TypeHandler<? extends T> typeHandler) {
    register((Type) javaType, typeHandler);
  }

  private static <T> void register(Type javaType, TypeHandler<? extends T> typeHandler) {
    register(javaType, null, typeHandler);
  }

  public static <T> void register(Class<T> type, JdbcType jdbcType, TypeHandler<? extends T> handler) {
    register((Type) type, jdbcType, handler);
  }

  private static void register(Type javaType, JdbcType jdbcType, TypeHandler<?> handler) {
    Map<JdbcType, TypeHandler<?>> map = TYPE_HANDLER_MAP.get(javaType);
    if (map == null) {
      map = new HashMap<JdbcType, TypeHandler<?>>();
      TYPE_HANDLER_MAP.put(javaType, map);
    }
    map.put(jdbcType, handler);
  }

}
