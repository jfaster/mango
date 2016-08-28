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

import org.jfaster.mango.base.concurrent.atomic.LongAddable;
import org.jfaster.mango.base.concurrent.atomic.LongAddables;
import org.jfaster.mango.base.sql.OperatorType;

/**
 * @author ash
 */
public class StatsCounter {

    private volatile ResettableStatsCounter resettableStatsCounter = new ResettableStatsCounter();

    /**
     * query or update or batchupdate
     */
    private OperatorType operatorType;

    /**
     * 是否使用缓存
     */
    private boolean isCacheable;

    /**
     * 缓存是否操作多个key
     */
    private boolean isUseMultipleKeys;

    /**
     * 是否缓存数据库中的null对象
     */
    private boolean isCacheNullObject;

    /**
     * 初始化统计，不可重置
     */
    private final LongAddable initCount = LongAddables.create();
    private final LongAddable totalInitTime = LongAddables.create();

    public void setOperatorType(OperatorType operatorType) {
        this.operatorType = operatorType;
    }

    public void setCacheable(boolean cacheable) {
        isCacheable = cacheable;
    }

    public void setUseMultipleKeys(boolean useMultipleKeys) {
        isUseMultipleKeys = useMultipleKeys;
    }

    public void setCacheNullObject(boolean cacheNullObject) {
        isCacheNullObject = cacheNullObject;
    }

    public void recordInit(long initTime) {
        if (initTime >= 0) {
            initCount.increment();
            totalInitTime.add(initTime);
        }
    }

    public void recordDatabaseExecuteSuccess(long executeTime) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordDatabaseExecuteSuccess(executeTime);
    }

    public void recordDatabaseExecuteException(long executeTime) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordDatabaseExecuteException(executeTime);
    }

    public void recordHits(int count) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordHits(count);
    }

    public void recordMisses(int count) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordMisses(count);
    }

    public void recordCacheGetSuccess(long executeTime) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordCacheGetSuccess(executeTime);
    }

    public void recordCacheGetException(long executeTime) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordCacheGetException(executeTime);
    }

    public void recordCacheGetBulkSuccess(long executeTime) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordCacheGetBulkSuccess(executeTime);
    }

    public void recordCacheGetBulkException(long executeTime) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordCacheGetBulkException(executeTime);
    }

    public void recordCacheSetSuccess(long executeTime) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordCacheSetSuccess(executeTime);
    }

    public void recordCacheSetException(long executeTime) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordCacheSetException(executeTime);
    }

    public void recordCacheAddSuccess(long executeTime) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordCacheAddSuccess(executeTime);
    }

    public void recordCacheAddException(long executeTime) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordCacheAddException(executeTime);
    }

    public void recordCacheDeleteSuccess(long executeTime) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordCacheDeleteSuccess(executeTime);
    }

    public void recordCacheDeleteException(long executeTime) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordCacheDeleteException(executeTime);
    }

    public void recordCacheBatchDeleteSuccess(long executeTime) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordCacheBatchDeleteSuccess(executeTime);
    }

    public void recordCacheBatchDeleteException(long executeTime) {
        final ResettableStatsCounter sc = resettableStatsCounter;
        sc.recordCacheBatchDeleteException(executeTime);
    }

    public OperatorStats snapshot() {
        final ResettableStatsCounter sc = resettableStatsCounter;
        return sc.snapshot(operatorType, isCacheable, isUseMultipleKeys, isCacheNullObject,
                initCount.sum(), totalInitTime.sum());
    }

    public synchronized void reset() {
        resettableStatsCounter = new ResettableStatsCounter();
    }

    private static class ResettableStatsCounter {

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

        public void recordDatabaseExecuteSuccess(long executeTime) {
            if (executeTime >= 0) {
                databaseExecuteSuccessCount.increment();
                totalDatabaseExecuteTime.add(executeTime);
            }
        }

        public void recordDatabaseExecuteException(long executeTime) {
            if (executeTime >= 0) {
                databaseExecuteExceptionCount.increment();
                totalDatabaseExecuteTime.add(executeTime);
            }
        }

        public void recordHits(int count) {
            if (count > 0) {
                hitCount.add(count);
            }
        }

        public void recordMisses(int count) {
            if (count > 0) {
                missCount.add(count);
            }
        }

        public void recordCacheGetSuccess(long executeTime) {
            if (executeTime >= 0) {
                cacheGetSuccessCount.increment();
                totalCacheGetTime.add(executeTime);
            }
        }

        public void recordCacheGetException(long executeTime) {
            if (executeTime >= 0) {
                cacheGetExceptionCount.increment();
                totalCacheGetTime.add(executeTime);
            }
        }

        public void recordCacheGetBulkSuccess(long executeTime) {
            if (executeTime >= 0) {
                cacheGetBulkSuccessCount.increment();
                totalCacheGetBulkTime.add(executeTime);
            }
        }

        public void recordCacheGetBulkException(long executeTime) {
            if (executeTime >= 0) {
                cacheGetBulkExceptionCount.increment();
                totalCacheGetBulkTime.add(executeTime);
            }
        }

        public void recordCacheSetSuccess(long executeTime) {
            if (executeTime >= 0) {
                cacheSetSuccessCount.increment();
                totalCacheSetTime.add(executeTime);
            }
        }

        public void recordCacheSetException(long executeTime) {
            if (executeTime >= 0) {
                cacheSetExceptionCount.increment();
                totalCacheSetTime.add(executeTime);
            }
        }

        public void recordCacheAddSuccess(long executeTime) {
            if (executeTime >= 0) {
                cacheAddSuccessCount.increment();
                totalCacheAddTime.add(executeTime);
            }
        }

        public void recordCacheAddException(long executeTime) {
            if (executeTime >= 0) {
                cacheAddExceptionCount.increment();
                totalCacheAddTime.add(executeTime);
            }
        }

        public void recordCacheDeleteSuccess(long executeTime) {
            if (executeTime >= 0) {
                cacheDeleteSuccessCount.increment();
                totalCacheDeleteTime.add(executeTime);
            }
        }

        public void recordCacheDeleteException(long executeTime) {
            if (executeTime >= 0) {
                cacheDeleteExceptionCount.increment();
                totalCacheDeleteTime.add(executeTime);
            }
        }

        public void recordCacheBatchDeleteSuccess(long executeTime) {
            if (executeTime >= 0) {
                cacheBatchDeleteSuccessCount.increment();
                totalCacheBatchDeleteTime.add(executeTime);
            }
        }

        public void recordCacheBatchDeleteException(long executeTime) {
            if (executeTime >= 0) {
                cacheBatchDeleteExceptionCount.increment();
                totalCacheBatchDeleteTime.add(executeTime);
            }
        }

        public OperatorStats snapshot(OperatorType operatorType,
                                      boolean isCacheable, boolean isUseMultipleKeys, boolean isCacheNullObject,
                                      long initCount, long totalInitTime) {
            return new OperatorStats(
                    operatorType,
                    isCacheable,
                    isUseMultipleKeys,
                    isCacheNullObject,
                    initCount,
                    totalInitTime,
                    databaseExecuteSuccessCount.sum(),
                    databaseExecuteExceptionCount.sum(),
                    totalDatabaseExecuteTime.sum(),
                    hitCount.sum(),
                    missCount.sum(),
                    cacheGetSuccessCount.sum(),
                    cacheGetExceptionCount.sum(),
                    totalCacheGetTime.sum(),
                    cacheGetBulkSuccessCount.sum(),
                    cacheGetBulkExceptionCount.sum(),
                    totalCacheGetBulkTime.sum(),
                    cacheSetSuccessCount.sum(),
                    cacheSetExceptionCount.sum(),
                    totalCacheSetTime.sum(),
                    cacheAddSuccessCount.sum(),
                    cacheAddExceptionCount.sum(),
                    totalCacheAddTime.sum(),
                    cacheDeleteSuccessCount.sum(),
                    cacheDeleteExceptionCount.sum(),
                    totalCacheDeleteTime.sum(),
                    cacheBatchDeleteSuccessCount.sum(),
                    cacheBatchDeleteExceptionCount.sum(),
                    totalCacheBatchDeleteTime.sum()
            );
        }

    }

}
