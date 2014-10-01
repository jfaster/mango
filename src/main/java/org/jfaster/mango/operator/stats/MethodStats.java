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

package org.jfaster.mango.operator.stats;

import javax.annotation.Nullable;

/**
 * db与cache的数据统计
 *
 * @author ash
 */
public class MethodStats {

    private final long initCount;
    private final long totalInitTime;
    private final long hitCount;
    private final long missCount;
    private final long executeSuccessCount;
    private final long executeExceptionCount;
    private final long totalExecuteTime;
    private final long evictionCount;

    public MethodStats() {
        initCount = 0;
        totalInitTime = 0;
        hitCount = 0;
        missCount = 0;
        executeSuccessCount = 0;
        executeExceptionCount = 0;
        totalExecuteTime = 0;
        evictionCount = 0;
    }

    public MethodStats(long initCount, long totalInitTime, long hitCount, long missCount,
                       long executeSuccessCount, long executeExceptionCount,
                       long totalExecuteTime, long evictionCount) {
        this.initCount = initCount;
        this.totalInitTime = totalInitTime;
        this.hitCount = hitCount;
        this.missCount = missCount;
        this.executeSuccessCount = executeSuccessCount;
        this.executeExceptionCount = executeExceptionCount;
        this.totalExecuteTime = totalExecuteTime;
        this.evictionCount = evictionCount;
    }

    /**
     * 返回平均初始化时间，单位为纳秒
     */
    public long averageInitPenalty() {
        return (initCount == 0)
                ? 0
                : totalInitTime / initCount;
    }

    /**
     * 初始化次数
     */
    public long initCount() {
        return initCount;
    }

    /**
     * 初始化总时间，单位为纳秒
     */
    public long totalInitTime() {
        return totalInitTime;
    }

    /**
     * 返回缓存命中数加缓存丢失数
     */
    public long requestCount() {
        return hitCount + missCount;
    }

    /**
     * 返回缓存命中数
     */
    public long hitCount() {
        return hitCount;
    }

    /**
     * 返回缓存命中率
     */
    public double hitRate() {
        long requestCount = requestCount();
        return (requestCount == 0) ? 1.0 : (double) hitCount / requestCount;
    }

    /**
     * 返回缓存丢失数
     */
    public long missCount() {
        return missCount;
    }

    /**
     * 返回缓存丢失率
     */
    public double missRate() {
        long requestCount = requestCount();
        return (requestCount == 0) ? 0.0 : (double) missCount / requestCount;
    }

    /**
     * 返回db执行总数
     */
    public long executeCount() {
        return executeSuccessCount + executeExceptionCount;
    }

    /**
     * 返回db执行成功数
     */
    public long executeSuccessCount() {
        return executeSuccessCount;
    }

    /**
     * 返回db执行成功率
     */
    public double executeSuccessRate() {
        long totalExecuteCount = executeSuccessCount + executeExceptionCount;
        return (totalExecuteCount == 0)
                ? 1.0
                : (double) executeSuccessCount / totalExecuteCount;
    }

    /**
     * 返回db执行失败数
     */
    public long executeExceptionCount() {
        return executeExceptionCount;
    }

    /**
     * 返回db执行失败率
     */
    public double executeExceptionRate() {
        long totalExecuteCount = executeSuccessCount + executeExceptionCount;
        return (totalExecuteCount == 0)
                ? 0.0
                : (double) executeExceptionCount / totalExecuteCount;
    }

    /**
     * 返回db执行总时间，单位为纳秒
     */
    public long totalExecuteTime() {
        return totalExecuteTime;
    }

    /**
     * 返回平均每次db执行时间，单位为纳秒
     */
    public long averageExecutePenalty() {
        long totalExecuteCount = executeSuccessCount + executeExceptionCount;
        return (totalExecuteCount == 0)
                ? 0
                : totalExecuteTime / totalExecuteCount;
    }

    /**
     * 删除cache数
     */
    public long evictionCount() {
        return evictionCount;
    }

    public MethodStats plus(MethodStats other) {
        return new MethodStats(
                initCount + other.initCount,
                totalInitTime + other.totalInitTime,
                hitCount + other.hitCount,
                missCount + other.missCount,
                executeSuccessCount + other.executeSuccessCount,
                executeExceptionCount + other.executeExceptionCount,
                totalExecuteTime + other.totalExecuteTime,
                evictionCount + other.evictionCount);
    }


    @Override
    public boolean equals(@Nullable Object object) {
        if (object instanceof MethodStats) {
            MethodStats other = (MethodStats) object;
            return hitCount == other.hitCount
                    && missCount == other.missCount
                    && executeSuccessCount == other.executeSuccessCount
                    && executeExceptionCount == other.executeExceptionCount
                    && totalExecuteTime == other.totalExecuteTime
                    && evictionCount == other.evictionCount;
        }
        return false;
    }

    @Override
    public String toString() {
        return String.format("{averageExecutePenalty=%s, executeSuccessCount=%s, executeExceptionCount=%s, " +
                "totalExecuteTime=%s, hitCount=%s, missCount=%s, evictionCount=%s, " +
                "averageInitPenalty=%s, initCount=%s, totalInitTime=%s}",
                averageExecutePenalty(), executeSuccessCount, executeExceptionCount,
                totalExecuteTime, hitCount, missCount, evictionCount,
                averageInitPenalty(), initCount, totalInitTime);
    }
}
