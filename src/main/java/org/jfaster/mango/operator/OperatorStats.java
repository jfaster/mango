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

package org.jfaster.mango.operator;

import java.lang.reflect.Method;

/**
 * db与cache的数据统计
 *
 * @author ash
 */
public class OperatorStats {

    private Method method;
    private final long initCount;
    private final long totalInitTime;
    private final long hitCount;
    private final long missCount;
    private final long executeSuccessCount;
    private final long executeExceptionCount;
    private final long totalExecuteTime;
    private final long evictionCount;

    public OperatorStats(long initCount, long totalInitTime, long hitCount, long missCount,
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

    public String getMethodName() {
        return method.getName();
    }

    public String getMethodNameWithParameterNum() {
        return method.getName() + "(" + method.getParameterTypes().length + ")";
    }

    public String getClassName() {
        return method.getDeclaringClass().getName();
    }

    public String getClassSimpleName() {
        return method.getDeclaringClass().getSimpleName();
    }

    /**
     * 返回平均初始化时间，单位为纳秒
     */
    public long getAverageInitPenalty() {
        return (initCount == 0)
                ? 0
                : totalInitTime / initCount;
    }

    /**
     * 初始化次数
     */
    public long getInitCount() {
        return initCount;
    }

    /**
     * 初始化总时间，单位为纳秒
     */
    public long getTotalInitTime() {
        return totalInitTime;
    }

    /**
     * 返回缓存命中数加缓存丢失数
     */
    public long getRequestCount() {
        return hitCount + missCount;
    }

    /**
     * 返回缓存命中数
     */
    public long getHitCount() {
        return hitCount;
    }

    /**
     * 返回缓存命中率
     */
    public double getHitRate() {
        long requestCount = getRequestCount();
        return (requestCount == 0) ? 1.0 : (double) hitCount / requestCount;
    }

    /**
     * 返回缓存丢失数
     */
    public long getMissCount() {
        return missCount;
    }

    /**
     * 返回缓存丢失率
     */
    public double getMissRate() {
        long requestCount = getRequestCount();
        return (requestCount == 0) ? 0.0 : (double) missCount / requestCount;
    }

    /**
     * 返回db执行总数
     */
    public long getExecuteCount() {
        return executeSuccessCount + executeExceptionCount;
    }

    /**
     * 返回db执行成功数
     */
    public long getExecuteSuccessCount() {
        return executeSuccessCount;
    }

    /**
     * 返回db执行成功率
     */
    public double getExecuteSuccessRate() {
        long totalExecuteCount = executeSuccessCount + executeExceptionCount;
        return (totalExecuteCount == 0)
                ? 1.0
                : (double) executeSuccessCount / totalExecuteCount;
    }

    /**
     * 返回db执行失败数
     */
    public long getExecuteExceptionCount() {
        return executeExceptionCount;
    }

    /**
     * 返回db执行失败率
     */
    public double getExecuteExceptionRate() {
        long totalExecuteCount = executeSuccessCount + executeExceptionCount;
        return (totalExecuteCount == 0)
                ? 0.0
                : (double) executeExceptionCount / totalExecuteCount;
    }

    /**
     * 返回db执行总时间，单位为纳秒
     */
    public long getTotalExecuteTime() {
        return totalExecuteTime;
    }

    /**
     * 返回平均每次db执行时间，单位为纳秒
     */
    public long getAverageExecutePenalty() {
        long totalExecuteCount = executeSuccessCount + executeExceptionCount;
        return (totalExecuteCount == 0)
                ? 0
                : totalExecuteTime / totalExecuteCount;
    }

    /**
     * 删除cache数
     */
    public long getEvictionCount() {
        return evictionCount;
    }

    void setMethod(Method method) {
        this.method = method;
    }

}
