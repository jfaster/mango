/*
 * Copyright 2014 mango.concurrent.cc
 *
 * The Netty Project licenses this file to you under the Apache License,
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

package cc.concurrent.mango.runtime.operator;

import cc.concurrent.mango.util.concurrent.atomic.LongAddable;
import cc.concurrent.mango.util.concurrent.atomic.LongAddables;

/**
 * @author ash
 */
public class SimpleStatsCounter implements StatsCounter {

    private final LongAddable hitCount = LongAddables.create();
    private final LongAddable missCount = LongAddables.create();
    private final LongAddable executeSuccessCount = LongAddables.create();
    private final LongAddable executeExceptionCount = LongAddables.create();
    private final LongAddable totalExecuteTime = LongAddables.create();
    private final LongAddable evictionCount = LongAddables.create();

    public SimpleStatsCounter() {}

    @Override
    public void recordHits(int count) {
        hitCount.add(count);
    }

    @Override
    public void recordMisses(int count) {
        missCount.add(count);
    }

    @Override
    public void recordExecuteSuccess(long executeTime) {
        executeSuccessCount.increment();
        totalExecuteTime.add(executeTime);
    }

    @Override
    public void recordExecuteException(long executeTime) {
        executeExceptionCount.increment();
        totalExecuteTime.add(executeTime);
    }

    @Override
    public void recordEviction(int count) {
        evictionCount.add(count);
    }

    @Override
    public void snapshot() {
        return;
    }

}
