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

package org.jfaster.mango.reflect.descriptor;

import org.jfaster.mango.annotation.*;
import org.jfaster.mango.base.Strings;
import org.jfaster.mango.exception.DescriptionException;

import javax.annotation.Nullable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Collections;
import java.util.List;

/**
 * 方法描述
 *
 * @author ash
 */
public class MethodDescriptor {

  private final Class<?> daoClass;
  private final ReturnDescriptor returnDescriptor;
  private final List<ParameterDescriptor> parameterDescriptors;

  private MethodDescriptor(
      Class<?> daoClass, ReturnDescriptor returnDescriptor, List<ParameterDescriptor> parameterDescriptors) {
    this.daoClass = daoClass;
    this.returnDescriptor = returnDescriptor;
    this.parameterDescriptors = Collections.unmodifiableList(parameterDescriptors);
  }

  public static MethodDescriptor create(
      Class<?> daoClass, ReturnDescriptor returnDescriptor, List<ParameterDescriptor> parameterDescriptors) {
    return new MethodDescriptor(daoClass, returnDescriptor, parameterDescriptors);
  }

  public String getSQL() {
    SQL sqlAnno = getAnnotation(SQL.class);
    if (sqlAnno == null) {
      throw new DescriptionException("each method expected one @SQL annotation but not found");
    }
    String sql = sqlAnno.value();
    if (Strings.isEmpty(sql)) {
      throw new DescriptionException("sql is null or empty");
    }
    return sql;
  }

  @Nullable
  public String getGlobalTable() {
    DB dbAnno = getAnnotation(DB.class);
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

  public String getDatabase() {
    DB dbAnno = getAnnotation(DB.class);
    if (dbAnno == null) {
      throw new DescriptionException("dao interface expected one @DB " +
          "annotation but not found");
    }
    return dbAnno.database();
  }

  @Nullable
  public Sharding getShardingAnno() {
    return getAnnotation(Sharding.class);
  }

  public boolean isUseCache() {
    CacheIgnored cacheIgnoredAnno = getAnnotation(CacheIgnored.class);
    Cache cacheAnno = getAnnotation(Cache.class);
    return cacheAnno != null && cacheIgnoredAnno == null;
  }

  public Class<?> getDaoClass() {
    return daoClass;
  }

  public boolean isAnnotationPresent(Class<? extends Annotation> annotationType) {
    return getAnnotation(annotationType) != null;
  }

  @Nullable
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return returnDescriptor.getAnnotation(annotationType);
  }

  public Type getReturnType() {
    return returnDescriptor.getType();
  }

  public Class<?> getReturnRawType() {
    return returnDescriptor.getRawType();
  }

  public List<Annotation> getAnnotations() {
    return returnDescriptor.getAnnotations();
  }

  public ReturnDescriptor getReturnDescriptor() {
    return returnDescriptor;
  }

  public List<ParameterDescriptor> getParameterDescriptors() {
    return parameterDescriptors;
  }

}
