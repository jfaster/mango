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
public class InvocationStatTest {

  @Test
  public void test() throws Exception {
    InvocationStat stat = InvocationStat.create();

    stat.recordDatabaseExecuteSuccess(2);
    assertThat(stat.getDatabaseExecuteSuccessCount(), equalTo(1L));
    assertThat(stat.getDatabaseExecuteExceptionCount(), equalTo(0L));
    assertThat(stat.getTotalDatabaseExecuteTime(), equalTo(2L));
    stat.recordDatabaseExecuteException(3);
    assertThat(stat.getDatabaseExecuteSuccessCount(), equalTo(1L));
    assertThat(stat.getDatabaseExecuteExceptionCount(), equalTo(1L));
    assertThat(stat.getTotalDatabaseExecuteTime(), equalTo(5L));

    stat.recordHits(4);
    assertThat(stat.getHitCount(), equalTo(4L));
    assertThat(stat.getMissCount(), equalTo(0L));
    stat.recordMisses(5);
    assertThat(stat.getHitCount(), equalTo(4L));
    assertThat(stat.getMissCount(), equalTo(5L));

    stat.recordCacheGetSuccess(6);
    assertThat(stat.getCacheGetSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheGetExceptionCount(), equalTo(0L));
    assertThat(stat.getTotalCacheGetTime(), equalTo(6L));
    stat.recordCacheGetException(7);
    assertThat(stat.getCacheGetSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheGetExceptionCount(), equalTo(1L));
    assertThat(stat.getTotalCacheGetTime(), equalTo(13L));

    stat.recordCacheGetBulkSuccess(8);
    assertThat(stat.getCacheGetBulkSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheGetBulkExceptionCount(), equalTo(0L));
    assertThat(stat.getTotalCacheGetBulkTime(), equalTo(8L));
    stat.recordCacheGetBulkException(9);
    assertThat(stat.getCacheGetBulkSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheGetBulkExceptionCount(), equalTo(1L));
    assertThat(stat.getTotalCacheGetBulkTime(), equalTo(17L));

    stat.recordCacheSetSuccess(10);
    assertThat(stat.getCacheSetSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheSetExceptionCount(), equalTo(0L));
    assertThat(stat.getTotalCacheSetTime(), equalTo(10L));
    stat.recordCacheSetException(11);
    assertThat(stat.getCacheSetSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheSetExceptionCount(), equalTo(1L));
    assertThat(stat.getTotalCacheSetTime(), equalTo(21L));

    stat.recordCacheAddSuccess(12);
    assertThat(stat.getCacheAddSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheAddExceptionCount(), equalTo(0L));
    assertThat(stat.getTotalCacheAddTime(), equalTo(12L));
    stat.recordCacheAddException(13);
    assertThat(stat.getCacheAddSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheAddExceptionCount(), equalTo(1L));
    assertThat(stat.getTotalCacheAddTime(), equalTo(25L));

    stat.recordCacheDeleteSuccess(14);
    assertThat(stat.getCacheDeleteSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheDeleteExceptionCount(), equalTo(0L));
    assertThat(stat.getTotalCacheDeleteTime(), equalTo(14L));
    stat.recordCacheDeleteException(15);
    assertThat(stat.getCacheDeleteSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheDeleteExceptionCount(), equalTo(1L));
    assertThat(stat.getTotalCacheDeleteTime(), equalTo(29L));

    stat.recordCacheBatchDeleteSuccess(16);
    assertThat(stat.getCacheBatchDeleteSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheBatchDeleteExceptionCount(), equalTo(0L));
    assertThat(stat.getTotalCacheBatchDeleteTime(), equalTo(16L));
    stat.recordCacheBatchDeleteException(17);
    assertThat(stat.getCacheBatchDeleteSuccessCount(), equalTo(1L));
    assertThat(stat.getCacheBatchDeleteExceptionCount(), equalTo(1L));
    assertThat(stat.getTotalCacheBatchDeleteTime(), equalTo(33L));
  }

}
