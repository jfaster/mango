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

import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class StatCollectorTest {

  @Test
  public void testMonitor() throws Exception {
    final Method m = StatCollectorTest.class.getDeclaredMethod("testMonitor");

    StatCollector sc = new StatCollector();
    final CountDownLatch cdl1 = new CountDownLatch(1);
    final CountDownLatch cdl2 = new CountDownLatch(1);
    final AtomicInteger t = new AtomicInteger();
    sc.initStatMonitor(new StatMonitor() {
      @Override
      public void handleStat(long statBeginTime, long statEndTime, List<OperatorStat> stats) throws Exception {
        int round = t.incrementAndGet();
        if (round == 1) {
          assertThat(stats.size(), equalTo(1));
          assertThat(stats.get(0).getDatabaseExecuteSuccessCount(), equalTo(2L));
          assertThat(stats.get(0).getTotalDatabaseExecuteTime(), equalTo(300L));
          cdl1.countDown();
        } else if (round == 2) {
          assertThat(stats.size(), equalTo(1));
          assertThat(stats.get(0).getDatabaseExecuteSuccessCount(), equalTo(1L));
          assertThat(stats.get(0).getTotalDatabaseExecuteTime(), equalTo(100L));
          cdl2.countDown();
        }
      }

      @Override
      public int periodSecond() {
        return 1;
      }
    });
    InvocationStat stat = InvocationStat.create();
    stat.recordDatabaseExecuteSuccess(100);
    stat.recordDatabaseExecuteSuccess(200);
    sc.getCombinedStat(m).getExecuteStat().accumulate(stat);
    cdl1.await();
    stat = InvocationStat.create();
    stat.recordDatabaseExecuteSuccess(100);
    sc.getCombinedStat(m).getExecuteStat().accumulate(stat);
    cdl2.await();

    sc.shutDown();
  }

}
