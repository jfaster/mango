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

import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author ash
 */
public class StatCollector {

  private final static InternalLogger logger = InternalLoggerFactory.getInstance(StatCollector.class);

  private final ConcurrentHashMap<Method, CombinedStat> combinedStatMap = new ConcurrentHashMap<Method, CombinedStat>();

  private long timestamp = currentTimeMillis();

  private ScheduledExecutorService scheduler;

  public synchronized void initStatMonitor(final StatMonitor statMonitor) {
    if (scheduler != null) {
      throw new IllegalStateException("StatMonitor is initialized many times");
    }
    scheduler = Executors.newSingleThreadScheduledExecutor();
    long period = statMonitor.getCheckPeriodSecond();
    scheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        try {
          StatInfo statInfo = resetAndGetStatInfo();
          statMonitor.check(statInfo.getStatStartTime(), statInfo.getStatEndTime(), statInfo.getStats());
        } catch (Exception e) {
          logger.error("StatMonitor check error", e);
        }
      }
    }, period, period, TimeUnit.SECONDS);
  }

  public synchronized StatInfo getStatInfo() {
    long now = currentTimeMillis();
    List<OperatorStat> operatorStats = new ArrayList<OperatorStat>();
    for (CombinedStat combinedStat : combinedStatMap.values()) {
      operatorStats.add(combinedStat.toOperatorStat());
    }
    return StatInfo.create(timestamp, now, operatorStats);
  }

  public synchronized StatInfo resetAndGetStatInfo() {
    long now = currentTimeMillis();
    List<CombinedStat> combinedStats = new ArrayList<CombinedStat>();
    for (CombinedStat combinedStat : combinedStatMap.values()) {
      final ExecuteStat executeStat = combinedStat.getExecuteStat();
      combinedStat.setExecuteStat(ExecuteStat.create());
      combinedStats.add(CombinedStat.create(combinedStat.getMetaStat(), combinedStat.getInitStat(), executeStat));
    }
    try {
      TimeUnit.MILLISECONDS.sleep(10); // 等待并发状态累加完成
    } catch (InterruptedException e) {
    }
    List<OperatorStat> operatorStats = new ArrayList<OperatorStat>();
    for (CombinedStat combinedStat : combinedStats) {
      operatorStats.add(combinedStat.toOperatorStat());
    }
    StatInfo statInfo = StatInfo.create(timestamp, now, operatorStats);
    timestamp = now;
    return statInfo;
  }

  public CombinedStat getCombinedStat(Method method) {
    CombinedStat stat = combinedStatMap.get(method);
    if (stat == null) {
      stat = CombinedStat.create();
      CombinedStat old = combinedStatMap.putIfAbsent(method, stat);
      if (old != null) { // 已经存在，就用老的，这样能保证单例
        stat = old;
      }
    }
    return stat;
  }

  private long currentTimeMillis() {
    return System.currentTimeMillis();
  }

}
