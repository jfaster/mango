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

package org.jfaster.mango.crud.named.factory;

import org.jfaster.mango.crud.CrudException;
import org.jfaster.mango.crud.CrudMeta;
import org.jfaster.mango.crud.named.builder.AbstractNamedBuilder;
import org.jfaster.mango.crud.named.builder.NamedDeleteBuilder;
import org.jfaster.mango.crud.named.parser.MethodNameInfo;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class NamedDeleteBuilderFactory extends AbstractNamedBuilderFactory {

  private final static List<String> PREFIXS = new ArrayList<String>();
  static {
    PREFIXS.add("deleteBy");
    PREFIXS.add("removeBy");
  }

  @Override
  public List<String> prefixs() {
    return PREFIXS;
  }

  @Override
  AbstractNamedBuilder createCustomBuilder(
          String methodName, List<Type> parameterTypes,
          Class<?> entityClass, MethodNameInfo info) {
    CrudMeta cm = new CrudMeta(entityClass);
    StringBuilder tailOfSql = new StringBuilder();
    buildWhereClause(tailOfSql, info.getOpUnits(), info.getLogics(), cm, parameterTypes, methodName, entityClass);
    if (info.getOrderUnit() != null) {
      throw new CrudException(""); // TODO
    }
    return new NamedDeleteBuilder(tailOfSql.toString());
  }

}
