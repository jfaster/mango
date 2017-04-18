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

package org.jfaster.mango.descriptor;

import org.jfaster.mango.annotation.*;
import org.jfaster.mango.exception.DescriptionException;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.reflect.Reflection;

import javax.annotation.Nullable;

/**
 * 方法描述工具类
 *
 * @author ash
 */
public class MethodDescriptors {

  public static String getSQL(MethodDescriptor md) {
    SQL sqlAnno = md.getAnnotation(SQL.class);
    String sql;
    if (sqlAnno != null) {
      sql = sqlAnno.value();
    } else {
      UseSqlGenerator useSqlGeneratorAnno = md.getAnnotation(UseSqlGenerator.class);
      if (useSqlGeneratorAnno == null) {
        throw new DescriptionException("each method expected one of @SQL or @UseSqlGenerator annotation but not found");
      }
      SqlGenerator sqlGenerator = Reflection.instantiateClass(useSqlGeneratorAnno.value());
      sql = sqlGenerator.generateSql(md);
    }
    if (Strings.isEmpty(sql)) {
      throw new DescriptionException("sql is null or empty");
    }
    return sql;
  }

  @Nullable
  public static String getGlobalTable(MethodDescriptor md) {
    DB dbAnno = md.getAnnotation(DB.class);
    if (dbAnno == null) {
      throw new DescriptionException("dao interface expected one @DB " +
          "annotation but not found");
    }
    String table = null;
    if (Strings.isNotEmpty(dbAnno.table())) {
      table = dbAnno.table();
    }
    return table;
  }

  public static String getDataSourceFactoryName(MethodDescriptor md) {
    DB dbAnno = md.getAnnotation(DB.class);
    if (dbAnno == null) {
      throw new DescriptionException("dao interface expected one @DB " +
          "annotation but not found");
    }
    return dbAnno.name();
  }

  @Nullable
  public static Sharding getShardingAnno(MethodDescriptor md) {
    return md.getAnnotation(Sharding.class);
  }

  public static boolean isUseCache(MethodDescriptor md) {
    CacheIgnored cacheIgnoredAnno = md.getAnnotation(CacheIgnored.class);
    Cache cacheAnno = md.getAnnotation(Cache.class);
    return cacheAnno != null && cacheIgnoredAnno == null;
  }

}
