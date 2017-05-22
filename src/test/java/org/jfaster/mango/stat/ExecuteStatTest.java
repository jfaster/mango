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

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class ExecuteStatTest {

  @Test
  public void testCreate() throws Exception {
    ExecuteStat stat = ExecuteStat.create();
    InvocationStat os = InvocationStat.create();

    os.recordDatabaseExecuteSuccess(2);
    os.recordDatabaseExecuteException(3);
    os.recordHits(4);
    os.recordMisses(5);
    os.recordCacheGetSuccess(6);
    os.recordCacheGetException(7);
    os.recordCacheGetBulkSuccess(8);
    os.recordCacheGetBulkException(9);
    os.recordCacheSetSuccess(10);
    os.recordCacheSetException(11);
    os.recordCacheAddSuccess(12);
    os.recordCacheAddException(13);
    os.recordCacheDeleteSuccess(14);
    os.recordCacheDeleteException(15);
    os.recordCacheBatchDeleteSuccess(16);
    os.recordCacheBatchDeleteException(17);
    
    stat.accumulate(os);

    assertThat(stat.getDatabaseExecuteSuccessCount(), equalTo(1L));
    assertThat(stat.getDatabaseExecuteExceptionCount(), equalTo(1L));
    assertThat(stat.getTotalDatabaseExecuteTime(), equalTo(5L));
    assertThat(stat.getHitCount(), equalTo(4L));
    assertThat(stat.getMissCount(), equalTo(5L));
    assertThat(stat.getCacheGetSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheGetExceptionCount(), equalTo(1L));
    assertThat(stat.getTotalCacheGetTime(), equalTo(13L));
    assertThat(stat.getCacheGetBulkSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheGetBulkExceptionCount(), equalTo(1L));
    assertThat(stat.getTotalCacheGetBulkTime(), equalTo(17L));
    assertThat(stat.getCacheSetSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheSetExceptionCount(), equalTo(1L));
    assertThat(stat.getTotalCacheSetTime(), equalTo(21L));
    assertThat(stat.getCacheAddSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheAddExceptionCount(), equalTo(1L));
    assertThat(stat.getTotalCacheAddTime(), equalTo(25L));
    assertThat(stat.getCacheDeleteSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheDeleteExceptionCount(), equalTo(1L));
    assertThat(stat.getTotalCacheDeleteTime(), equalTo(29L));
    assertThat(stat.getCacheBatchDeleteSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheBatchDeleteExceptionCount(), equalTo(1L));
    assertThat(stat.getTotalCacheBatchDeleteTime(), equalTo(33L));
    
  }

}
