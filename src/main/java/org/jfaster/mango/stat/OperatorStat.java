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

import org.jfaster.mango.util.jdbc.OperatorType;

import java.lang.reflect.Method;

/**
 * @author ash
 */
public class OperatorStat {

  private final Class<?> daoClass;
  private final Method method;

  private final String sql;

  /**
   * query or update or batchupdate
   */
  private final OperatorType operatorType;

  /**
   * 是否使用缓存
   */
  private final boolean isCacheable;

  /**
   * 缓存是否操作多个key
   */
  private final boolean isUseMultipleKeys;

  /**
   * 是否缓存数据库中的null对象
   */
  private boolean isCacheNullObject;

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

  public OperatorStat(
      Class<?> daoClass,
      Method method,
      String sql,
      OperatorType operatorType,
      boolean isCacheable,
      boolean isUseMultipleKeys,
      boolean isCacheNullObject,
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
    this.daoClass = daoClass;
    this.method = method;
    this.sql = sql;
    this.operatorType = operatorType;
    this.isCacheable = isCacheable;
    this.isUseMultipleKeys = isUseMultipleKeys;
    this.isCacheNullObject = isCacheNullObject;
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

  public Class<?> getDaoClass() {
    return daoClass;
  }

  public Method getMethod() {
    return method;
  }

  public String getSql() {
    return sql;
  }

  public OperatorType getOperatorType() {
    return operatorType;
  }

  public boolean isCacheable() {
    return isCacheable;
  }

  public boolean isUseMultipleKeys() {
    return isUseMultipleKeys;
  }

  public boolean isCacheNullObject() {
    return isCacheNullObject;
  }

  public long getInitCount() {
    return initCount;
  }

  public long getTotalInitTime() {
    return totalInitTime;
  }

  /**
   * 返回平均初始化时间，单位为纳秒
   */
  public long getInitAveragePenalty() {
    return (initCount == 0)
        ? 0
        : totalInitTime / initCount;
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

  /**
   * 返回db执行总数
   */
  public long getDatabaseExecuteCount() {
    return databaseExecuteSuccessCount + databaseExecuteExceptionCount;
  }

  /**
   * 返回db执行成功率
   */
  public double getDatabaseExecuteSuccessRate() {
    long totalDatabaseExecuteCount = getDatabaseExecuteCount();
    return (totalDatabaseExecuteCount == 0)
        ? 1.0
        : (double) databaseExecuteSuccessCount / totalDatabaseExecuteCount;
  }

  /**
   * 返回db执行失败率
   */
  public double getDatabaseExecuteExceptionRate() {
    long totalDatabaseExecuteCount = getDatabaseExecuteCount();
    return (totalDatabaseExecuteCount == 0)
        ? 0.0
        : (double) databaseExecuteExceptionCount / totalDatabaseExecuteCount;
  }

  /**
   * 返回平均每次db执行时间，单位为纳秒
   */
  public long getDatabaseExecuteAveragePenalty() {
    long totalDatabaseExecuteCount = getDatabaseExecuteCount();
    return (totalDatabaseExecuteCount == 0)
        ? 0
        : totalDatabaseExecuteTime / totalDatabaseExecuteCount;
  }

  public long getHitCount() {
    return hitCount;
  }

  public long getMissCount() {
    return missCount;
  }

  /**
   * 返回缓存命中率
   */
  public double getHitRate() {
    long requestCount = hitCount + missCount;
    return (requestCount == 0) ? 1.0 : (double) hitCount / requestCount;
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

  /**
   * 返回cache[get]执行总数
   */
  public long getCacheGetCount() {
    return cacheGetSuccessCount + cacheGetExceptionCount;
  }

  /**
   * 返回cache[get]执行成功率
   */
  public double getCacheGetSuccessRate() {
    long totalCacheGetCount = getCacheGetCount();
    return (totalCacheGetCount == 0)
        ? 1.0
        : (double) cacheGetSuccessCount / totalCacheGetCount;
  }

  /**
   * 返回cache[get]执行失败率
   */
  public double getCacheGetExceptionRate() {
    long totalCacheGetCount = getCacheGetCount();
    return (totalCacheGetCount == 0)
        ? 0.0
        : (double) cacheGetExceptionCount / totalCacheGetCount;
  }

  /**
   * 返回平均每次cache[get]执行时间，单位为纳秒
   */
  public long getCacheGetAveragePenalty() {
    long totalCacheGetCount = getCacheGetCount();
    return (totalCacheGetCount == 0)
        ? 0
        : totalCacheGetTime / totalCacheGetCount;
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

  /**
   * 返回cache[getBulk]执行总数
   */
  public long getCacheGetBulkCount() {
    return cacheGetBulkSuccessCount + cacheGetBulkExceptionCount;
  }

  /**
   * 返回cache[getBulk]执行成功率
   */
  public double getCacheGetBulkSuccessRate() {
    long totalCacheGetBulkCount = getCacheGetBulkCount();
    return (totalCacheGetBulkCount == 0)
        ? 1.0
        : (double) cacheGetBulkSuccessCount / totalCacheGetBulkCount;
  }

  /**
   * 返回cache[getBulk]执行失败率
   */
  public double getCacheGetBulkExceptionRate() {
    long totalCacheGetBulkCount = getCacheGetBulkCount();
    return (totalCacheGetBulkCount == 0)
        ? 0.0
        : (double) cacheGetBulkExceptionCount / totalCacheGetBulkCount;
  }

  /**
   * 返回平均每次cache[getBulk]执行时间，单位为纳秒
   */
  public long getCacheGetBulkAveragePenalty() {
    long totalCacheGetBulkCount = getCacheGetBulkCount();
    return (totalCacheGetBulkCount == 0)
        ? 0
        : totalCacheGetBulkTime / totalCacheGetBulkCount;
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

  /**
   * 返回cache[set]执行总数
   */
  public long getCacheSetCount() {
    return cacheSetSuccessCount + cacheSetExceptionCount;
  }

  /**
   * 返回cache[set]执行成功率
   */
  public double getCacheSetSuccessRate() {
    long totalCacheSetCount = getCacheSetCount();
    return (totalCacheSetCount == 0)
        ? 1.0
        : (double) cacheSetSuccessCount / totalCacheSetCount;
  }

  /**
   * 返回cache[set]执行失败率
   */
  public double getCacheSetExceptionRate() {
    long totalCacheSetCount = getCacheSetCount();
    return (totalCacheSetCount == 0)
        ? 0.0
        : (double) cacheSetExceptionCount / totalCacheSetCount;
  }

  /**
   * 返回平均每次cache[set]执行时间，单位为纳秒
   */
  public long getCacheSetAveragePenalty() {
    long totalCacheSetCount = getCacheSetCount();
    return (totalCacheSetCount == 0)
        ? 0
        : totalCacheSetTime / totalCacheSetCount;
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

  /**
   * 返回cache[add]执行总数
   */
  public long getCacheAddCount() {
    return cacheAddSuccessCount + cacheAddExceptionCount;
  }

  /**
   * 返回cache[add]执行成功率
   */
  public double getCacheAddSuccessRate() {
    long totalCacheAddCount = getCacheAddCount();
    return (totalCacheAddCount == 0)
        ? 1.0
        : (double) cacheAddSuccessCount / totalCacheAddCount;
  }

  /**
   * 返回cache[add]执行失败率
   */
  public double getCacheAddExceptionRate() {
    long totalCacheAddCount = getCacheAddCount();
    return (totalCacheAddCount == 0)
        ? 0.0
        : (double) cacheAddExceptionCount / totalCacheAddCount;
  }

  /**
   * 返回平均每次cache[add]执行时间，单位为纳秒
   */
  public long getCacheAddAveragePenalty() {
    long totalCacheAddCount = getCacheAddCount();
    return (totalCacheAddCount == 0)
        ? 0
        : totalCacheAddTime / totalCacheAddCount;
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

  /**
   * 返回cache[delete]执行总数
   */
  public long getCacheDeleteCount() {
    return cacheDeleteSuccessCount + cacheDeleteExceptionCount;
  }

  /**
   * 返回cache[delete]执行成功率
   */
  public double getCacheDeleteSuccessRate() {
    long totalCacheDeleteCount = getCacheDeleteCount();
    return (totalCacheDeleteCount == 0)
        ? 1.0
        : (double) cacheDeleteSuccessCount / totalCacheDeleteCount;
  }

  /**
   * 返回cache[delete]执行失败率
   */
  public double getCacheDeleteExceptionRate() {
    long totalCacheDeleteCount = getCacheDeleteCount();
    return (totalCacheDeleteCount == 0)
        ? 0.0
        : (double) cacheDeleteExceptionCount / totalCacheDeleteCount;
  }

  /**
   * 返回平均每次cache[delete]执行时间，单位为纳秒
   */
  public long getCacheDeleteAveragePenalty() {
    long totalCacheDeleteCount = getCacheDeleteCount();
    return (totalCacheDeleteCount == 0)
        ? 0
        : totalCacheDeleteTime / totalCacheDeleteCount;
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

  /**
   * 返回cache[batchDelete]执行总数
   */
  public long getCacheBatchDeleteCount() {
    return cacheBatchDeleteSuccessCount + cacheBatchDeleteExceptionCount;
  }

  /**
   * 返回cache[batchDelete]执行成功率
   */
  public double getCacheBatchDeleteSuccessRate() {
    long totalCacheBatchDeleteCount = getCacheBatchDeleteCount();
    return (totalCacheBatchDeleteCount == 0)
        ? 1.0
        : (double) cacheBatchDeleteSuccessCount / totalCacheBatchDeleteCount;
  }

  /**
   * 返回cache[batchDelete]执行失败率
   */
  public double getCacheBatchDeleteExceptionRate() {
    long totalCacheBatchDeleteCount = getCacheBatchDeleteCount();
    return (totalCacheBatchDeleteCount == 0)
        ? 0.0
        : (double) cacheBatchDeleteExceptionCount / totalCacheBatchDeleteCount;
  }

  /**
   * 返回平均每次cache[batchDelete]执行时间，单位为纳秒
   */
  public long getCacheBatchDeleteAveragePenalty() {
    long totalCacheBatchDeleteCount = getCacheBatchDeleteCount();
    return (totalCacheBatchDeleteCount == 0)
        ? 0
        : totalCacheBatchDeleteTime / totalCacheBatchDeleteCount;
  }

}
