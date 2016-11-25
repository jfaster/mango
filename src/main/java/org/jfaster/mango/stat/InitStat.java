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

import org.jfaster.mango.stat.atomic.LongAddable;
import org.jfaster.mango.stat.atomic.LongAddables;

/**
 * @author ash
 */
public class InitStat {

  private final LongAddable initCount = LongAddables.create();
  private final LongAddable totalInitTime = LongAddables.create();

  private InitStat() {
  }

  public static InitStat create() {
    return new InitStat();
  }

  public void recordInit(long initTime) {
    if (initTime >= 0) {
      initCount.increment();
      totalInitTime.add(initTime);
    }
  }

  public long getInitCount() {
    return initCount.sum();
  }

  public long getTotalInitTime() {
    return totalInitTime.sum();
  }
}
