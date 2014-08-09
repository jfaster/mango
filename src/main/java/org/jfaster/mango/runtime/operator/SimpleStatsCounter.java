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

package org.jfaster.mango.runtime.operator;

import org.jfaster.mango.MethodStats;
import org.jfaster.mango.util.concurrent.atomic.LongAddable;
import org.jfaster.mango.util.concurrent.atomic.LongAddables;

/**
 * @author ash
 */
public class SimpleStatsCounter implements StatsCounter {

    private final LongAddable initCount = LongAddables.create();
    private final LongAddable totalInitTime = LongAddables.create();
    private final LongAddable hitCount = LongAddables.create();
    private final LongAddable missCount = LongAddables.create();
    private final LongAddable executeSuccessCount = LongAddables.create();
    private final LongAddable executeExceptionCount = LongAddables.create();
    private final LongAddable totalExecuteTime = LongAddables.create();
    private final LongAddable evictionCount = LongAddables.create();

    public SimpleStatsCounter() {}

    @Override
    public void recordInit(long initTime) {
        if (initTime > 0) {
            initCount.increment();
            totalInitTime.add(initTime);
        }
    }

    @Override
    public void recordHits(int count) {
        if (count > 0) {
            hitCount.add(count);
        }
    }

    @Override
    public void recordMisses(int count) {
        if (count > 0) {
            missCount.add(count);
        }
    }

    @Override
    public void recordExecuteSuccess(long executeTime) {
        if (executeTime > 0) {
            executeSuccessCount.increment();
            totalExecuteTime.add(executeTime);
        }
    }

    @Override
    public void recordExecuteException(long executeTime) {
        if (executeTime > 0) {
            executeExceptionCount.increment();
            totalExecuteTime.add(executeTime);
        }
    }

    @Override
    public void recordEviction(int count) {
        if (count > 0) {
            evictionCount.add(count);
        }
    }

    @Override
    public MethodStats snapshot() {
        return new MethodStats(
                initCount.sum(),
                totalInitTime.sum(),
                hitCount.sum(),
                missCount.sum(),
                executeSuccessCount.sum(),
                executeExceptionCount.sum(),
                totalExecuteTime.sum(),
                evictionCount.sum());
    }

}
