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
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 监控datasource
 *
 * @author ash
 */
public class DataSourceMonitor {

  private static final ConcurrentHashMap<DataSource, AtomicInteger> map =
      new ConcurrentHashMap<DataSource, AtomicInteger>();

  private static volatile boolean forceCheckAutoCommit = false;

  public static boolean needCheckAutoCommit(DataSource ds) {
    return forceCheckAutoCommit || map.get(ds) != null;
  }

  public static void resetAutoCommitFail(DataSource ds) {
    AtomicInteger val = map.get(ds);
    if (val == null) {
      val = new AtomicInteger();
      AtomicInteger old = map.putIfAbsent(ds, val);
      if (old != null) {
        val = old;
      }
    }
    val.incrementAndGet();
  }

  public static void setForceCheckAutoCommit(boolean forceCheckAutoCommit) {
    DataSourceMonitor.forceCheckAutoCommit = forceCheckAutoCommit;
  }

  public static Map<DataSource, Integer> getFailedDataSources() {
    Map<DataSource, Integer> dsMap = new HashMap<DataSource, Integer>();
    for (Map.Entry<DataSource, AtomicInteger> entry : map.entrySet()) {
      dsMap.put(entry.getKey(), entry.getValue().intValue());
    }
    return dsMap;
  }

}
