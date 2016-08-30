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

package org.jfaster.mango.transaction;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ash
 */
public abstract class TransactionSynchronizationManager {

  private static final ThreadLocal<Map<DataSource, ConnectionHolder>> CONNECTION_HOLDERS =
      new ThreadLocal<Map<DataSource, ConnectionHolder>>();

  public static void bindConnectionHolder(DataSource dataSource, ConnectionHolder connHolder) {
    Map<DataSource, ConnectionHolder> map = CONNECTION_HOLDERS.get();
    if (map == null) {
      map = new HashMap<DataSource, ConnectionHolder>();
      CONNECTION_HOLDERS.set(map);
    }
    ConnectionHolder oldConnHolder = map.put(dataSource, connHolder);
    if (oldConnHolder != null) {
      throw new IllegalStateException("Already ConnectionHolder [" + oldConnHolder + "] for DataSource [" +
          dataSource + "] bound to thread [" + Thread.currentThread().getName() + "]");
    }
  }

  public static void unbindConnectionHolder(DataSource dataSource) {
    Map<DataSource, ConnectionHolder> map = CONNECTION_HOLDERS.get();
    if (map == null) {
      throw new IllegalStateException(
          "No value for DataSource [" + dataSource + "] bound to " +
              "thread [" + Thread.currentThread().getName() + "]");
    }
    ConnectionHolder connHolder = map.remove(dataSource);
    if (map.isEmpty()) {
      CONNECTION_HOLDERS.remove();
    }
    if (connHolder == null) {
      throw new IllegalStateException(
          "No value for DataSource [" + dataSource + "] bound to " +
              "thread [" + Thread.currentThread().getName() + "]");
    }
  }

  public static ConnectionHolder getConnectionHolder(DataSource dataSource) {
    Map<DataSource, ConnectionHolder> map = CONNECTION_HOLDERS.get();
    return map == null ? null : map.get(dataSource);
  }

}






