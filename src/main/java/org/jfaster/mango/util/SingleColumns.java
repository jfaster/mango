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

package org.jfaster.mango.util;

import java.util.HashSet;
import java.util.Set;

/**
 * @author ash
 */
public final class SingleColumns {

  private final static Set<Class<?>> singleColumClassSet = new HashSet<Class<?>>();

  static {
    // 字符串
    singleColumClassSet.add(String.class);

    // 特殊类型
    singleColumClassSet.add(java.math.BigDecimal.class);
    singleColumClassSet.add(java.math.BigInteger.class);
    singleColumClassSet.add(java.util.Date.class);

    // jdbc中的类型
    singleColumClassSet.add(byte[].class);
    singleColumClassSet.add(java.sql.Date.class);
    singleColumClassSet.add(java.sql.Time.class);
    singleColumClassSet.add(java.sql.Timestamp.class);
    singleColumClassSet.add(java.sql.Blob.class);
    singleColumClassSet.add(java.sql.Clob.class);

    // 基本数据类型
    for (Class<?> type : Primitives.allPrimitiveTypes()) { // int.class等
      singleColumClassSet.add(type);
    }
    for (Class<?> type : Primitives.allWrapperTypes()) { // Integer.class等
      singleColumClassSet.add(type);
    }
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

}
