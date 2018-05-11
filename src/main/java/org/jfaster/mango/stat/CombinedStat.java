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
public class CombinedStat {

  private final MetaStat metaStat;

  private final InitStat initStat;

  private volatile ExecuteStat executeStat;

  private CombinedStat(MetaStat metaStat, InitStat initStat, ExecuteStat executeStat) {
    this.metaStat = metaStat;
    this.initStat = initStat;
    this.executeStat = executeStat;
  }

  public static CombinedStat create() {
    return create(MetaStat.create(), InitStat.create(), ExecuteStat.create());
  }

  public static CombinedStat create(MetaStat metaStat, InitStat initStat, ExecuteStat executeStat) {
    return new CombinedStat(metaStat, initStat, executeStat);
  }

  public OperatorStat toOperatorStat() {
    final ExecuteStat es = executeStat;
    return new OperatorStat(
        metaStat.getDaoClass(),
        metaStat.getMethod(),
        metaStat.getSql(),
        metaStat.getOperatorType(),
        metaStat.isCacheable(),
        metaStat.isUseMultipleKeys(),
        metaStat.isCacheNullObject(),
        initStat.getInitCount(),
        initStat.getTotalInitTime(),
        es.getDatabaseExecuteSuccessCount(),
        es.getDatabaseExecuteExceptionCount(),
        es.getTotalDatabaseExecuteTime(),
        es.getHitCount(),
        es.getMissCount(),
        es.getCacheGetSuccessCount(),
        es.getCacheGetExceptionCount(),
        es.getTotalCacheGetTime(),
        es.getCacheGetBulkSuccessCount(),
        es.getCacheGetBulkExceptionCount(),
        es.getTotalCacheGetBulkTime(),
        es.getCacheSetSuccessCount(),
        es.getCacheSetExceptionCount(),
        es.getTotalCacheSetTime(),
        es.getCacheAddSuccessCount(),
        es.getCacheAddExceptionCount(),
        es.getTotalCacheAddTime(),
        es.getCacheDeleteSuccessCount(),
        es.getCacheDeleteExceptionCount(),
        es.getTotalCacheDeleteTime(),
        es.getCacheBatchDeleteSuccessCount(),
        es.getCacheBatchDeleteExceptionCount(),
        es.getTotalCacheBatchDeleteTime()
    );
  }

  public MetaStat getMetaStat() {
    return metaStat;
  }

  public InitStat getInitStat() {
    return initStat;
  }

  public ExecuteStat getExecuteStat() {
    return executeStat;
  }

  public void setExecuteStat(ExecuteStat executeStat) {
    this.executeStat = executeStat;
  }

}
