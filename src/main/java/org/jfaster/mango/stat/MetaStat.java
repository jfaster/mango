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

package org.jfaster.mango.stat;

import org.jfaster.mango.util.jdbc.OperatorType;

import java.lang.reflect.Method;

/**
 * @author ash
 */
public class MetaStat {

  /**
   * DAO接口所在的类
   */
  private Class<?> daoClass;

  /**
   * DAO接口所在方法
   */
  private Method method;

  /**
   * SQL，自定义或自动生成
   */
  private String sql;

  /**
   * query or update or batchupdate
   */
  private OperatorType operatorType;

  /**
   * 是否使用缓存
   */
  private boolean isCacheable;

  /**
   * 缓存是否操作多个key
   */
  private boolean isUseMultipleKeys;

  /**
   * 是否缓存数据库中的null对象
   */
  private boolean isCacheNullObject;

  private MetaStat() {
  }

  public static MetaStat create() {
    return new MetaStat();
  }

  public Class<?> getDaoClass() {
    return daoClass;
  }

  public void setDaoClass(Class<?> daoClass) {
    this.daoClass = daoClass;
  }

  public Method getMethod() {
    return method;
  }

  public void setMethod(Method method) {
    this.method = method;
  }

  public String getSql() {
    return sql;
  }

  public void setSql(String sql) {
    this.sql = sql;
  }

  public OperatorType getOperatorType() {
    return operatorType;
  }

  public void setOperatorType(OperatorType operatorType) {
    this.operatorType = operatorType;
  }

  public boolean isCacheable() {
    return isCacheable;
  }

  public void setCacheable(boolean cacheable) {
    isCacheable = cacheable;
  }

  public boolean isUseMultipleKeys() {
    return isUseMultipleKeys;
  }

  public void setUseMultipleKeys(boolean useMultipleKeys) {
    isUseMultipleKeys = useMultipleKeys;
  }

  public boolean isCacheNullObject() {
    return isCacheNullObject;
  }

  public void setCacheNullObject(boolean cacheNullObject) {
    isCacheNullObject = cacheNullObject;
  }

}
