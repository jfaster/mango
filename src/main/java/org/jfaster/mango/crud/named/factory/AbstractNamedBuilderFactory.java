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

import org.jfaster.mango.crud.Builder;
import org.jfaster.mango.crud.BuilderFactory;
import org.jfaster.mango.crud.CrudException;
import org.jfaster.mango.crud.CrudMeta;
import org.jfaster.mango.crud.named.builder.AbstractNamedBuilder;
import org.jfaster.mango.crud.named.parser.*;
import org.jfaster.mango.crud.named.parser.op.Op;
import org.jfaster.mango.crud.named.parser.op.Param1ForCollectionOp;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.reflect.TypeToken;
import org.jfaster.mango.util.reflect.TypeWrapper;
import org.jfaster.mango.util.reflect.Types;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author ash
 */
public abstract class AbstractNamedBuilderFactory extends BuilderFactory {

  @Nullable
  @Override
  public Builder doTryGetBuilder(String name, Type returnType, List<Type> parameterTypes, Class<?> entityClass, Class<?> idClass) {
    int matchSize = metchSize(name);
    if (matchSize == 0) {
      return null;
    }
    String str = name.substring(matchSize);
    MethodNameInfo info = MethodNameParser.parse(str);
    return createCustomBuilder(name, parameterTypes, entityClass, info);
  }

  public abstract List<String> prefixs();

  abstract AbstractNamedBuilder createCustomBuilder(
          String methodName, List<Type> parameterTypes,
          Class<?> entityClass, MethodNameInfo info);

  private int metchSize(String name) {
    for (String prefix : prefixs()) {
      if (Strings.isEmpty(prefix)) {
        throw new IllegalStateException("prefix can't be empty");
      }
      Pattern p = Pattern.compile(prefix + "[A-Z]");
      Matcher m = p.matcher(name);
      if (m.find() && m.start() == 0) {
        return prefix.length();
      }
    }
    return 0;
  }

  private int getCountOfOpUnitParams(List<OpUnit> opUnits) {
    int countOfOpUnitParams = 0;

    for (OpUnit opUnit : opUnits) {
      countOfOpUnitParams += opUnit.getOp().paramCount();
    }

    return countOfOpUnitParams;
  }

  protected void buildWhereClause(
      StringBuilder tailOfSql, List<OpUnit> opUnits, List<String> logics,
      CrudMeta cm, List<Type> parameterTypes, String methodName, Class<?> clazz) {
    int countOfOpUnitParams;

    if (opUnits.size() == 0) {
      throw new IllegalStateException(); // TODO msg
    }
    if (opUnits.size() != (logics.size() + 1)) {
      throw new IllegalStateException(); // TODO msg
    }

    countOfOpUnitParams = getCountOfOpUnitParams(opUnits);

    if (parameterTypes.size() < countOfOpUnitParams) {
      throw new CrudException("the name of method [" + methodName + "] is error, " +
          "the number of parameters expected greater or equal than " + countOfOpUnitParams + ", but " + parameterTypes.size());
    }
    tailOfSql.append("where ");
    int paramIndex = 1;
    for (int i = 0; i < opUnits.size(); i++) {
      OpUnit opUnit = opUnits.get(i);
      String property = opUnit.getProperty();
      String column = cm.getColumnByProperty(property);
      Type propertyType = cm.getTypeByProperty(property);
      if (column == null || propertyType == null) {
        throw new CrudException("the name of method [" + methodName + "] is error, " +
            "property " + property + " can't be found in '" + clazz + "'");
      }
      Op op = opUnit.getOp();
      String[] params = new String[op.paramCount()];
      for (int j = 0; j < params.length; j++) {
        Type parameterType = parameterTypes.get(paramIndex - 1);
        checkType(parameterType, propertyType, paramIndex, methodName, op);
        params[j] = ":" + paramIndex;
        paramIndex++;
      }
      tailOfSql.append(op.render(column, params));
      if (i != (opUnits.size() - 1)) {
        tailOfSql.append(" ").append(logics.get(i)).append(" ");
      }
    }
  }

  protected void checkType(Type paramType, Type propType, int paramIndex, String methodName, Op op) {
    Class<?> rawPropType = TypeToken.of(propType).getRawType();
    if (!(op instanceof Param1ForCollectionOp)) {
      Class<?> rawParamType = TypeToken.of(paramType).getRawType();
      if (!Types.equals(rawPropType, rawParamType)) {
        throw new CrudException("the type of " + paramIndex + "th parameters of method [" + methodName + "] " +
            "expected '" + propType + "', but '" + paramType + "'");
      }
    } else { // in (:1) 类型需特殊处理
      // TODO msg
      TypeWrapper tw = new TypeWrapper(paramType);
      if (!tw.canIterable()) {
        throw new CrudException("the type of " + paramIndex + "th parameters of method [" + methodName + "] " +
                "expected iterable, but '" + paramType + "'");
      }
      if (!Types.equals(rawPropType, tw.getMappedClass())) {
        throw new CrudException("the type of " + paramIndex + "th parameters of method [" + methodName + "] error");
      }
    }
  }

  protected void buildOrderByClause(
      StringBuilder tailOfSql, @Nullable  OrderUnit orderUnit,
      CrudMeta cm, String methodName, Class<?> clazz) {
    if (orderUnit != null) {
      String property = orderUnit.getProperty();
      String column = cm.getColumnByProperty(property);
      if (column == null) {
        throw new CrudException("the name of method [" + methodName + "] is error, " +
            "property " + property + " can't be found in '" + clazz + "'");
      }
      tailOfSql.append(" order by " + column);
      OrderType orderType = orderUnit.getOrderType();
      if (orderType != OrderType.NONE) {
        tailOfSql.append(" " + orderType.toString().toLowerCase());
      }
    }
  }

}
