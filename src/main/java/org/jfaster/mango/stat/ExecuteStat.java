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
public class ExecuteStat {

  /**
   * 数据库执行统计
   */
  private final LongAddable databaseExecuteSuccessCount = LongAddables.create();
  private final LongAddable databaseExecuteExceptionCount = LongAddables.create();
  private final LongAddable totalDatabaseExecuteTime = LongAddables.create();

  /**
   * 缓存命中统计
   */
  private final LongAddable hitCount = LongAddables.create();
  private final LongAddable missCount = LongAddables.create();

  /**
   * 缓存get统计
   */
  private final LongAddable cacheGetSuccessCount = LongAddables.create();
  private final LongAddable cacheGetExceptionCount = LongAddables.create();
  private final LongAddable totalCacheGetTime = LongAddables.create();

  /**
   * 缓存getBulk统计
   */
  private final LongAddable cacheGetBulkSuccessCount = LongAddables.create();
  private final LongAddable cacheGetBulkExceptionCount = LongAddables.create();
  private final LongAddable totalCacheGetBulkTime = LongAddables.create();

  /**
   * 缓存set统计
   */
  private final LongAddable cacheSetSuccessCount = LongAddables.create();
  private final LongAddable cacheSetExceptionCount = LongAddables.create();
  private final LongAddable totalCacheSetTime = LongAddables.create();

  /**
   * 缓存add统计
   */
  private final LongAddable cacheAddSuccessCount = LongAddables.create();
  private final LongAddable cacheAddExceptionCount = LongAddables.create();
  private final LongAddable totalCacheAddTime = LongAddables.create();

  /**
   * 缓存delete统计
   */
  private final LongAddable cacheDeleteSuccessCount = LongAddables.create();
  private final LongAddable cacheDeleteExceptionCount = LongAddables.create();
  private final LongAddable totalCacheDeleteTime = LongAddables.create();

  /**
   * 缓存batchDelete统计
   */
  private final LongAddable cacheBatchDeleteSuccessCount = LongAddables.create();
  private final LongAddable cacheBatchDeleteExceptionCount = LongAddables.create();
  private final LongAddable totalCacheBatchDeleteTime = LongAddables.create();

  private ExecuteStat() {
  }

  public static ExecuteStat create() {
    return new ExecuteStat();
  }

  public void accumulate(InvocationStat stat) {
    handleDatabaseExecute(stat);
    handleGetAndMiss(stat);
    handleCacheGet(stat);
    handleCacheGetBulk(stat);
    handleCacheSet(stat);
    handleCacheAdd(stat);
    handleCacheDelete(stat);
    handleCacheBatchDelete(stat);
  }

  private void handleDatabaseExecute(InvocationStat stat) {
    if (stat.getDatabaseExecuteSuccessCount() > 0) {
      databaseExecuteSuccessCount.add(stat.getDatabaseExecuteSuccessCount());
    }
    if (stat.getDatabaseExecuteExceptionCount() > 0) {
      databaseExecuteExceptionCount.add(stat.getDatabaseExecuteExceptionCount());
    }
    if (stat.getTotalDatabaseExecuteTime() > 0) {
      totalDatabaseExecuteTime.add(stat.getTotalDatabaseExecuteTime());
    }
  }

  private void handleGetAndMiss(InvocationStat stat) {
    if (stat.getHitCount() > 0) {
      hitCount.add(stat.getHitCount());
    }
    if (stat.getMissCount() > 0) {
      missCount.add(stat.getMissCount());
    }
  }

  private void handleCacheGet(InvocationStat stat) {
    if (stat.getCacheGetSuccessCount() > 0) {
      cacheGetSuccessCount.add(stat.getCacheGetSuccessCount());
    }
    if (stat.getCacheGetExceptionCount() > 0) {
      cacheGetExceptionCount.add(stat.getCacheGetExceptionCount());
    }
    if (stat.getTotalCacheGetTime() > 0) {
      totalCacheGetTime.add(stat.getTotalCacheGetTime());
    }
  }

  private void handleCacheGetBulk(InvocationStat stat) {
    if (stat.getCacheGetBulkSuccessCount() > 0) {
      cacheGetBulkSuccessCount.add(stat.getCacheGetBulkSuccessCount());
    }
    if (stat.getCacheGetBulkExceptionCount() > 0) {
      cacheGetBulkExceptionCount.add(stat.getCacheGetBulkExceptionCount());
    }
    if (stat.getTotalCacheGetBulkTime() > 0) {
      totalCacheGetBulkTime.add(stat.getTotalCacheGetBulkTime());
    }
  }

  private void handleCacheSet(InvocationStat stat) {
    if (stat.getCacheSetSuccessCount() > 0) {
      cacheSetSuccessCount.add(stat.getCacheSetSuccessCount());
    }
    if (stat.getCacheSetExceptionCount() > 0) {
      cacheSetExceptionCount.add(stat.getCacheSetExceptionCount());
    }
    if (stat.getTotalCacheSetTime() > 0) {
      totalCacheSetTime.add(stat.getTotalCacheSetTime());
    }
  }

  private void handleCacheAdd(InvocationStat stat) {
    if (stat.getCacheAddSuccessCount() > 0) {
      cacheAddSuccessCount.add(stat.getCacheAddSuccessCount());
    }
    if (stat.getCacheAddExceptionCount() > 0) {
      cacheAddExceptionCount.add(stat.getCacheAddExceptionCount());
    }
    if (stat.getTotalCacheAddTime() > 0) {
      totalCacheAddTime.add(stat.getTotalCacheAddTime());
    }
  }

  private void handleCacheDelete(InvocationStat stat) {
    if (stat.getCacheDeleteSuccessCount() > 0) {
      cacheDeleteSuccessCount.add(stat.getCacheDeleteSuccessCount());
    }
    if (stat.getCacheDeleteExceptionCount() > 0) {
      cacheDeleteExceptionCount.add(stat.getCacheDeleteExceptionCount());
    }
    if (stat.getTotalCacheDeleteTime() > 0) {
      totalCacheDeleteTime.add(stat.getTotalCacheDeleteTime());
    }
  }

  private void handleCacheBatchDelete(InvocationStat stat) {
    if (stat.getCacheBatchDeleteSuccessCount() > 0) {
      cacheBatchDeleteSuccessCount.add(stat.getCacheBatchDeleteSuccessCount());
    }
    if (stat.getCacheBatchDeleteExceptionCount() > 0) {
      cacheBatchDeleteExceptionCount.add(stat.getCacheBatchDeleteExceptionCount());
    }
    if (stat.getTotalCacheBatchDeleteTime() > 0) {
      totalCacheBatchDeleteTime.add(stat.getTotalCacheBatchDeleteTime());
    }
  }

  public long getDatabaseExecuteSuccessCount() {
    return databaseExecuteSuccessCount.sum();
  }

  public long getDatabaseExecuteExceptionCount() {
    return databaseExecuteExceptionCount.sum();
  }

  public long getTotalDatabaseExecuteTime() {
    return totalDatabaseExecuteTime.sum();
  }

  public long getHitCount() {
    return hitCount.sum();
  }

  public long getMissCount() {
    return missCount.sum();
  }

  public long getCacheGetSuccessCount() {
    return cacheGetSuccessCount.sum();
  }

  public long getCacheGetExceptionCount() {
    return cacheGetExceptionCount.sum();
  }

  public long getTotalCacheGetTime() {
    return totalCacheGetTime.sum();
  }

  public long getCacheGetBulkSuccessCount() {
    return cacheGetBulkSuccessCount.sum();
  }

  public long getCacheGetBulkExceptionCount() {
    return cacheGetBulkExceptionCount.sum();
  }

  public long getTotalCacheGetBulkTime() {
    return totalCacheGetBulkTime.sum();
  }

  public long getCacheSetSuccessCount() {
    return cacheSetSuccessCount.sum();
  }

  public long getCacheSetExceptionCount() {
    return cacheSetExceptionCount.sum();
  }

  public long getTotalCacheSetTime() {
    return totalCacheSetTime.sum();
  }

  public long getCacheAddSuccessCount() {
    return cacheAddSuccessCount.sum();
  }

  public long getCacheAddExceptionCount() {
    return cacheAddExceptionCount.sum();
  }

  public long getTotalCacheAddTime() {
    return totalCacheAddTime.sum();
  }

  public long getCacheDeleteSuccessCount() {
    return cacheDeleteSuccessCount.sum();
  }

  public long getCacheDeleteExceptionCount() {
    return cacheDeleteExceptionCount.sum();
  }

  public long getTotalCacheDeleteTime() {
    return totalCacheDeleteTime.sum();
  }

  public long getCacheBatchDeleteSuccessCount() {
    return cacheBatchDeleteSuccessCount.sum();
  }

  public long getCacheBatchDeleteExceptionCount() {
    return cacheBatchDeleteExceptionCount.sum();
  }

  public long getTotalCacheBatchDeleteTime() {
    return totalCacheBatchDeleteTime.sum();
  }
}
