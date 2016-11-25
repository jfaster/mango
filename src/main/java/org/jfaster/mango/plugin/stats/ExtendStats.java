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

package org.jfaster.mango.plugin.stats;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.annotation.Sharding;
import org.jfaster.mango.sharding.NotUseTableShardingStrategy;
import org.jfaster.mango.stat.OperatorStat;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.ToStringHelper;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class ExtendStats {

  private OperatorStat operatorStat;

  private Method method;

  public ExtendStats(OperatorStat operatorStat) {
    this.operatorStat = operatorStat;
    this.method = operatorStat.getMethod();
  }

  public String getSimpleClassName() {
    return method.getDeclaringClass().getSimpleName();
  }

  public String getSimpleMethodName() {
    return method.getName() + "(" + method.getParameterTypes().length + ")";
  }

  public String getSql() {
    String sql = method.getAnnotation(SQL.class).value();
    DB dbAnno = method.getDeclaringClass().getAnnotation(DB.class);
    String table = dbAnno.table();
    if (Strings.isNotEmpty(table)) {
      Sharding shardingAnno = method.getAnnotation(Sharding.class);
      if (shardingAnno == null) {
        shardingAnno = method.getDeclaringClass().getAnnotation(Sharding.class);
      }
      if (shardingAnno != null &&
          !NotUseTableShardingStrategy.class.equals(shardingAnno.tableShardingStrategy())) {
        table = table + "_#";
      }
      sql = sql.replaceAll("#table", table);
    }
    return sql;
  }

  public List<String> getStrParameterTypes() {
    List<String> r = new ArrayList<String>();
    for (Type type : method.getGenericParameterTypes()) {
      r.add(ToStringHelper.toString(type));
    }
    return r;
  }

  public String getType() {
    return operatorStat.getOperatorType().name().toLowerCase();
  }

}
