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

package org.jfaster.mango.binding;

import org.jfaster.mango.type.TypeHandler;
import org.jfaster.mango.type.TypeHandlerRegistry;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class BoundSql {

  private String sql;
  private final List<Object> args;
  private final List<TypeHandler<?>> typeHandlers;

  public BoundSql(String sql) {
    this.sql = sql;
    this.args = new ArrayList<Object>();
    this.typeHandlers = new ArrayList<TypeHandler<?>>();
  }

  public BoundSql(String sql, List<Object> args, List<TypeHandler<?>> typeHandlers) {
    this.sql = sql;
    this.args = args;
    this.typeHandlers = typeHandlers;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public void addArg(Object obj) {
    if (obj == null) {
      throw new IllegalArgumentException("arg can't be null, if arg is null please use method addNullArg");
    }
    TypeHandler<?> typeHandler = TypeHandlerRegistry.getTypeHandler(obj.getClass());
    args.add(obj);
    typeHandlers.add(typeHandler);
  }

  public void addArg(int index, Object obj) {
    if (obj == null) {
      throw new IllegalArgumentException("arg can't be null, if arg is null please use method addNullArg");
    }
    TypeHandler<?> typeHandler = TypeHandlerRegistry.getTypeHandler(obj.getClass());
    args.add(index, obj);
    typeHandlers.add(index, typeHandler);
  }

  public void addNullArg(Class<?> type) {
    TypeHandler<?> typeHandler = TypeHandlerRegistry.getTypeHandler(type);
    args.add(null);
    typeHandlers.add(typeHandler);
  }

  public void addNullArg(int index, Class<?> type) {
    TypeHandler<?> typeHandler = TypeHandlerRegistry.getTypeHandler(type);
    args.add(index, null);
    typeHandlers.add(index, typeHandler);
  }

  public List<Object> getArgs() {
    return args;
  }

  public List<TypeHandler<?>> getTypeHandlers() {
    return typeHandlers;
  }

  public BoundSql copy() {
    List<Object> args = new ArrayList<Object>();
    for (Object arg : getArgs()) {
      args.add(arg);
    }
    List<TypeHandler<?>> typeHandlers = new ArrayList<TypeHandler<?>>();
    for (TypeHandler<?> typeHandler : getTypeHandlers()) {
      typeHandlers.add(typeHandler);
    }
    return new BoundSql(getSql(), args, typeHandlers);
  }

}
