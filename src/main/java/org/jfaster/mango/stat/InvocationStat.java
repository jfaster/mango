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

/**
 * @author ash
 */
public class InvocationStat {

  /**
   * 数据库执行统计
   */
  private long databaseExecuteSuccessCount;
  private long databaseExecuteExceptionCount;
  private long totalDatabaseExecuteTime;

  /**
   * 缓存命中统计
   */
  private long hitCount;
  private long missCount;

  /**
   * 缓存get统计
   */
  private long cacheGetSuccessCount;
  private long cacheGetExceptionCount;
  private long totalCacheGetTime;

  /**
   * 缓存getBulk统计
   */
  private long cacheGetBulkSuccessCount;
  private long cacheGetBulkExceptionCount;
  private long totalCacheGetBulkTime;

  /**
   * 缓存set统计
   */
  private long cacheSetSuccessCount;
  private long cacheSetExceptionCount;
  private long totalCacheSetTime;

  /**
   * 缓存add统计
   */
  private long cacheAddSuccessCount;
  private long cacheAddExceptionCount;
  private long totalCacheAddTime;

  /**
   * 缓存delete统计
   */
  private long cacheDeleteSuccessCount;
  private long cacheDeleteExceptionCount;
  private long totalCacheDeleteTime;

  /**
   * 缓存batchDelete统计
   */
  private long cacheBatchDeleteSuccessCount;
  private long cacheBatchDeleteExceptionCount;
  private long totalCacheBatchDeleteTime;
  
  private InvocationStat() {
  }

  public static InvocationStat create() {
    return new InvocationStat();
  }

  public void recordDatabaseExecuteSuccess(long executeTime) {
    if (executeTime >= 0) {
      databaseExecuteSuccessCount++;
      totalDatabaseExecuteTime += executeTime;
    }
  }

  public void recordDatabaseExecuteException(long executeTime) {
    if (executeTime >= 0) {
      databaseExecuteExceptionCount++;
      totalDatabaseExecuteTime += executeTime;
    }
  }

  public void recordHits(int count) {
    if (count > 0) {
      hitCount += count;
    }
  }

  public void recordMisses(int count) {
    if (count > 0) {
      missCount += count;
    }
  }

  public void recordCacheGetSuccess(long executeTime) {
    if (executeTime >= 0) {
      cacheGetSuccessCount++;
      totalCacheGetTime += executeTime;
    }
  }

  public void recordCacheGetException(long executeTime) {
    if (executeTime >= 0) {
      cacheGetExceptionCount++;
      totalCacheGetTime += executeTime;
    }
  }

  public void recordCacheGetBulkSuccess(long executeTime) {
    if (executeTime >= 0) {
      cacheGetBulkSuccessCount++;
      totalCacheGetBulkTime += executeTime;
    }
  }

  public void recordCacheGetBulkException(long executeTime) {
    if (executeTime >= 0) {
      cacheGetBulkExceptionCount++;
      totalCacheGetBulkTime += executeTime;
    }
  }

  public void recordCacheSetSuccess(long executeTime) {
    if (executeTime >= 0) {
      cacheSetSuccessCount++;
      totalCacheSetTime += executeTime;
    }
  }

  public void recordCacheSetException(long executeTime) {
    if (executeTime >= 0) {
      cacheSetExceptionCount++;
      totalCacheSetTime += executeTime;
    }
  }

  public void recordCacheAddSuccess(long executeTime) {
    if (executeTime >= 0) {
      cacheAddSuccessCount++;
      totalCacheAddTime += executeTime;
    }
  }

  public void recordCacheAddException(long executeTime) {
    if (executeTime >= 0) {
      cacheAddExceptionCount++;
      totalCacheAddTime += executeTime;
    }
  }

  public void recordCacheDeleteSuccess(long executeTime) {
    if (executeTime >= 0) {
      cacheDeleteSuccessCount++;
      totalCacheDeleteTime += executeTime;
    }
  }

  public void recordCacheDeleteException(long executeTime) {
    if (executeTime >= 0) {
      cacheDeleteExceptionCount++;
      totalCacheDeleteTime += executeTime;
    }
  }

  public void recordCacheBatchDeleteSuccess(long executeTime) {
    if (executeTime >= 0) {
      cacheBatchDeleteSuccessCount++;
      totalCacheBatchDeleteTime += executeTime;
    }
  }

  public void recordCacheBatchDeleteException(long executeTime) {
    if (executeTime >= 0) {
      cacheBatchDeleteExceptionCount++;
      totalCacheBatchDeleteTime += executeTime;
    }
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

}
