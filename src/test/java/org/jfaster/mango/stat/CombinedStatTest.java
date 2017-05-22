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
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class CombinedStatTest {
  
  @Test
  public void test() throws Exception {
    Method m = CombinedStatTest.class.getDeclaredMethod("test");
    assertThat(m, notNullValue());
    
    CombinedStat stat = CombinedStat.create();
    
    MetaStat metaStat = stat.getMetaStat();
    metaStat.setMethod(m);
    metaStat.setOperatorType(OperatorType.UPDATE);
    metaStat.setCacheable(true);
    metaStat.setUseMultipleKeys(true);
    metaStat.setCacheNullObject(true);

    InitStat initStat = stat.getInitStat();
    initStat.recordInit(1000);

    ExecuteStat executeStat = stat.getExecuteStat();
    InvocationStat oneExecuteStat = InvocationStat.create();
    oneExecuteStat.recordDatabaseExecuteSuccess(2);
    oneExecuteStat.recordDatabaseExecuteException(3);
    oneExecuteStat.recordHits(4);
    oneExecuteStat.recordMisses(5);
    oneExecuteStat.recordCacheGetSuccess(6);
    oneExecuteStat.recordCacheGetException(7);
    oneExecuteStat.recordCacheGetBulkSuccess(8);
    oneExecuteStat.recordCacheGetBulkException(9);
    oneExecuteStat.recordCacheSetSuccess(10);
    oneExecuteStat.recordCacheSetException(11);
    oneExecuteStat.recordCacheAddSuccess(12);
    oneExecuteStat.recordCacheAddException(13);
    oneExecuteStat.recordCacheDeleteSuccess(14);
    oneExecuteStat.recordCacheDeleteException(15);
    oneExecuteStat.recordCacheBatchDeleteSuccess(16);
    oneExecuteStat.recordCacheBatchDeleteException(17);
    executeStat.accumulate(oneExecuteStat);

    OperatorStat operatorStat = stat.toOperatorStat();
    assertThat(operatorStat.getMethod(), equalTo(m));
    assertThat(operatorStat.getOperatorType(), equalTo(OperatorType.UPDATE));
    assertThat(operatorStat.isCacheable(), equalTo(true));
    assertThat(operatorStat.isUseMultipleKeys(), equalTo(true));
    assertThat(operatorStat.isCacheNullObject(), equalTo(true));
    assertThat(operatorStat.getInitCount(), equalTo(1L));
    assertThat(operatorStat.getTotalInitTime(), equalTo(1000L));
    assertThat(operatorStat.getDatabaseExecuteSuccessCount(), equalTo(1L));
    assertThat(operatorStat.getDatabaseExecuteExceptionCount(), equalTo(1L));
    assertThat(operatorStat.getTotalDatabaseExecuteTime(), equalTo(5L));
    assertThat(operatorStat.getHitCount(), equalTo(4L));
    assertThat(operatorStat.getMissCount(), equalTo(5L));
    assertThat(operatorStat.getCacheGetSuccessCount(), equalTo(1L));
    assertThat(operatorStat.getCacheGetExceptionCount(), equalTo(1L));
    assertThat(operatorStat.getTotalCacheGetTime(), equalTo(13L));
    assertThat(operatorStat.getCacheGetBulkSuccessCount(), equalTo(1L));
    assertThat(operatorStat.getCacheGetBulkExceptionCount(), equalTo(1L));
    assertThat(operatorStat.getTotalCacheGetBulkTime(), equalTo(17L));
    assertThat(operatorStat.getCacheSetSuccessCount(), equalTo(1L));
    assertThat(operatorStat.getCacheSetExceptionCount(), equalTo(1L));
    assertThat(operatorStat.getTotalCacheSetTime(), equalTo(21L));
    assertThat(operatorStat.getCacheAddSuccessCount(), equalTo(1L));
    assertThat(operatorStat.getCacheAddExceptionCount(), equalTo(1L));
    assertThat(operatorStat.getTotalCacheAddTime(), equalTo(25L));
    assertThat(operatorStat.getCacheDeleteSuccessCount(), equalTo(1L));
    assertThat(operatorStat.getCacheDeleteExceptionCount(), equalTo(1L));
    assertThat(operatorStat.getTotalCacheDeleteTime(), equalTo(29L));
    assertThat(operatorStat.getCacheBatchDeleteSuccessCount(), equalTo(1L));
    assertThat(operatorStat.getCacheBatchDeleteExceptionCount(), equalTo(1L));
    assertThat(operatorStat.getTotalCacheBatchDeleteTime(), equalTo(33L));
  }
  
}
