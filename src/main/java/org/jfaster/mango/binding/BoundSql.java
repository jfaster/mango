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

  private final StringBuilder sql;
  private final List<Object> args;
  private final List<TypeHandler<?>> typeHandlers;

  public BoundSql(StringBuilder sql) {
    this.sql = sql;
    this.args = new ArrayList<>();
    this.typeHandlers = new ArrayList<>();
  }

  public BoundSql(StringBuilder sql, List<Object> args, List<TypeHandler<?>> typeHandlers) {
    this.sql = sql;
    this.args = args;
    this.typeHandlers = typeHandlers;
  }

  public String getSql() {
    return sql.toString();
  }

  public BoundSql append(String str) {
    sql.append(str);
    return this;
  }

  public BoundSql prepend(String str) {
    sql.insert(0, str);
    return this;
  }

  public BoundSql append(Object obj) {
    sql.append(String.valueOf(obj));
    return this;
  }

  public BoundSql prepend(Object obj) {
    sql.insert(0, String.valueOf(obj));
    return this;
  }

  public void addArg(Object obj) {
    if (obj == null) {
      throw new IllegalArgumentException("arg can't be null");
    }
    TypeHandler<?> typeHandler = TypeHandlerRegistry.getTypeHandler(obj.getClass());
    args.add(obj);
    typeHandlers.add(typeHandler);
  }

  public List<Object> getArgs() {
    return args;
  }

  public List<TypeHandler<?>> getTypeHandlers() {
    return typeHandlers;
  }

  public BoundSql copy() {
    List<Object> args = new ArrayList<>();
    for (Object arg : getArgs()) {
      args.add(arg);
    }
    List<TypeHandler<?>> typeHandlers = new ArrayList<>();
    for (TypeHandler<?> typeHandler : getTypeHandlers()) {
      typeHandlers.add(typeHandler);
    }
    return new BoundSql(new StringBuilder(sql.toString()), args, typeHandlers);
  }

}
