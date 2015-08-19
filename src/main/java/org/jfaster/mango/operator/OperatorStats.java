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

    /**
     * query or update or batchupdate
     */
    private final OperatorType type;

    /**
     * 初始化统计
     */
    private final long initCount;
    private final long totalInitTime;

    /**
     * 数据库执行统计
     */
    private final long databaseExecuteSuccessCount;
    private final long databaseExecuteExceptionCount;
    private final long totalDatabaseExecuteTime;

    /**
     * 缓存命中统计
     */
    private final long hitCount;
    private final long missCount;

    /**
     * 缓存get统计
     */
    private final long cacheGetSuccessCount;
    private final long cacheGetExceptionCount;
    private final long totalCacheGetTime;

    /**
     * 缓存getBulk统计
     */
    private final long cacheGetBulkSuccessCount;
    private final long cacheGetBulkExceptionCount;
    private final long totalCacheGetBulkTime;

    /**
     * 缓存set统计
     */
    private final long cacheSetSuccessCount;
    private final long cacheSetExceptionCount;
    private final long totalCacheSetTime;

    /**
     * 缓存add统计
     */
    private final long cacheAddSuccessCount;
    private final long cacheAddExceptionCount;
    private final long totalCacheAddTime;

    /**
     * 缓存delete统计
     */
    private final long cacheDeleteSuccessCount;
    private final long cacheDeleteExceptionCount;
    private final long totalCacheDeleteTime;

    /**
     * 缓存batchDelete统计
     */
    private final long cacheBatchDeleteSuccessCount;
    private final long cacheBatchDeleteExceptionCount;
    private final long totalCacheBatchDeleteTime;


    public OperatorStats(
            OperatorType type,
            long initCount,
            long totalInitTime,
            long databaseExecuteSuccessCount,
            long databaseExecuteExceptionCount,
            long totalDatabaseExecuteTime,
            long hitCount,
            long missCount,
            long cacheGetSuccessCount,
            long cacheGetExceptionCount,
            long totalCacheGetTime,
            long cacheGetBulkSuccessCount,
            long cacheGetBulkExceptionCount,
            long totalCacheGetBulkTime,
            long cacheSetSuccessCount,
            long cacheSetExceptionCount,
            long totalCacheSetTime,
            long cacheAddSuccessCount,
            long cacheAddExceptionCount,
            long totalCacheAddTime,
            long cacheDeleteSuccessCount,
            long cacheDeleteExceptionCount,
            long totalCacheDeleteTime,
            long cacheBatchDeleteSuccessCount,
            long cacheBatchDeleteExceptionCount,
            long totalCacheBatchDeleteTime) {
        this.type = type;
        this.initCount = initCount;
        this.totalInitTime = totalInitTime;
        this.databaseExecuteSuccessCount = databaseExecuteSuccessCount;
        this.databaseExecuteExceptionCount = databaseExecuteExceptionCount;
        this.totalDatabaseExecuteTime = totalDatabaseExecuteTime;
        this.hitCount = hitCount;
        this.missCount = missCount;
        this.cacheGetSuccessCount = cacheGetSuccessCount;
        this.cacheGetExceptionCount = cacheGetExceptionCount;
        this.totalCacheGetTime = totalCacheGetTime;
        this.cacheGetBulkSuccessCount = cacheGetBulkSuccessCount;
        this.cacheGetBulkExceptionCount = cacheGetBulkExceptionCount;
        this.totalCacheGetBulkTime = totalCacheGetBulkTime;
        this.cacheSetSuccessCount = cacheSetSuccessCount;
        this.cacheSetExceptionCount = cacheSetExceptionCount;
        this.totalCacheSetTime = totalCacheSetTime;
        this.cacheAddSuccessCount = cacheAddSuccessCount;
        this.cacheAddExceptionCount = cacheAddExceptionCount;
        this.totalCacheAddTime = totalCacheAddTime;
        this.cacheDeleteSuccessCount = cacheDeleteSuccessCount;
        this.cacheDeleteExceptionCount = cacheDeleteExceptionCount;
        this.totalCacheDeleteTime = totalCacheDeleteTime;
        this.cacheBatchDeleteSuccessCount = cacheBatchDeleteSuccessCount;
        this.cacheBatchDeleteExceptionCount = cacheBatchDeleteExceptionCount;
        this.totalCacheBatchDeleteTime = totalCacheBatchDeleteTime;
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

    public OperatorType getType() {
        return type;
    }

    public long getInitCount() {
        return initCount;
    }

    public long getTotalInitTime() {
        return totalInitTime;
    }

    public long getDatabaseExecuteSuccessCount() {
        return databaseExecuteSuccessCount;
    }

    public long getDatabaseExecuteExceptionCount() {
        return databaseExecuteExceptionCount;
    }

    public long getTotalDatabaseExecuteTime() {
        return totalDatabaseExecuteTime;
    }

    public long getHitCount() {
        return hitCount;
    }

    public long getMissCount() {
        return missCount;
    }

    public long getCacheGetSuccessCount() {
        return cacheGetSuccessCount;
    }

    public long getCacheGetExceptionCount() {
        return cacheGetExceptionCount;
    }

    public long getTotalCacheGetTime() {
        return totalCacheGetTime;
    }

    public long getCacheGetBulkSuccessCount() {
        return cacheGetBulkSuccessCount;
    }

    public long getCacheGetBulkExceptionCount() {
        return cacheGetBulkExceptionCount;
    }

    public long getTotalCacheGetBulkTime() {
        return totalCacheGetBulkTime;
    }

    public long getCacheSetSuccessCount() {
        return cacheSetSuccessCount;
    }

    public long getCacheSetExceptionCount() {
        return cacheSetExceptionCount;
    }

    public long getTotalCacheSetTime() {
        return totalCacheSetTime;
    }

    public long getCacheAddSuccessCount() {
        return cacheAddSuccessCount;
    }

    public long getCacheAddExceptionCount() {
        return cacheAddExceptionCount;
    }

    public long getTotalCacheAddTime() {
        return totalCacheAddTime;
    }

    public long getCacheDeleteSuccessCount() {
        return cacheDeleteSuccessCount;
    }

    public long getCacheDeleteExceptionCount() {
        return cacheDeleteExceptionCount;
    }

    public long getTotalCacheDeleteTime() {
        return totalCacheDeleteTime;
    }

    public long getCacheBatchDeleteSuccessCount() {
        return cacheBatchDeleteSuccessCount;
    }

    public long getCacheBatchDeleteExceptionCount() {
        return cacheBatchDeleteExceptionCount;
    }

    public long getTotalCacheBatchDeleteTime() {
        return totalCacheBatchDeleteTime;
    }

    void setMethod(Method method) {
        this.method = method;
    }

}
