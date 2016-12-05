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
import java.util.concurrent.*;

/**
 * @author ash
 */
public class StatCollector {

  private final static InternalLogger logger = InternalLoggerFactory.getInstance(StatCollector.class);

  private final ConcurrentHashMap<Method, CombinedStat> combinedStatMap = new ConcurrentHashMap<Method, CombinedStat>();

  private long timestamp = currentTimeMillis();

  private ScheduledExecutorService scheduler;

  private ExecutorService worker;

  public synchronized void initStatMonitor(final StatMonitor statMonitor) {
    if (scheduler != null) {
      throw new IllegalStateException("StatMonitor is initialized many times");
    }
    scheduler = Executors.newSingleThreadScheduledExecutor();
    worker = Executors.newSingleThreadExecutor();
    long periodSecond = statMonitor.periodSecond();
    long nowSecond = currentTimeMillis() / 1000;
    long delay = (nowSecond / periodSecond) * periodSecond + periodSecond - nowSecond; // 对齐时间
    scheduler.scheduleAtFixedRate(new Runnable() {
      @Override
      public void run() {
        final StatInfo statInfo = resetAndGetStatInfo();
        worker.execute(new Runnable() {
          @Override
          public void run() {
            try {
              statMonitor.handleStat(statInfo.getStatBeginTime(), statInfo.getStatEndTime(), statInfo.getStats());
            } catch (Exception e) {
              e.printStackTrace();
              logger.error("StatMonitor handle stat error", e);
            }
          }
        });
      }
    }, delay, periodSecond, TimeUnit.SECONDS);
  }

  public synchronized void shutDown() {
    if (scheduler != null) {
      scheduler.shutdown();
    }
    if (worker != null) {
      worker.shutdown();
    }
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
