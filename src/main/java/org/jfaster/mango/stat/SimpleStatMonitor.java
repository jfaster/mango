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

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * @author ash
 */
public class SimpleStatMonitor implements StatMonitor {

  private static final String NEWLINE = System.getProperty("line.separator");

  private static final int DEFAULT_PERIOD_SECOND = 10;

  private int periodSecond;

  public SimpleStatMonitor() {
    this(DEFAULT_PERIOD_SECOND);
  }

  public SimpleStatMonitor(int periodSecond) {
    this.periodSecond = periodSecond;
  }

  @Override
  public void handleStat(long statBeginTime, long statEndTime, List<OperatorStat> stats) throws Exception {
    StringBuilder data = new StringBuilder();
    data.append("Performance Statistics  [")
        .append(format(statBeginTime))
        .append("] - [")
        .append(format(statEndTime))
        .append("]")
        .append(NEWLINE);
    data.append(String.format("%-36s%-12s%-12s%-12s%n",
        "dao", "avg", "total", "error"));
    for (OperatorStat stat : stats) {
      if (stat.getDatabaseExecuteCount() > 0) { // 执行db数大于0才统计
      String dao = stat.getMethod().getDeclaringClass().getSimpleName() + "." + stat.getMethod().getName();
      data.append(String.format("%-36s%-12.1f%-12s%-12s%n",
          dao,
          (double) stat.getDatabaseExecuteAveragePenalty() / (1000*1000),
          stat.getDatabaseExecuteCount(),
          stat.getDatabaseExecuteExceptionCount()));
      }
    }
    System.out.println(data);
  }

  @Override
  public int periodSecond() {
    return periodSecond;
  }

  private String format(long time) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    return format.format(new Date(time));
  }

  public void setPeriodSecond(int periodSecond) {
    this.periodSecond = periodSecond;
  }
}
