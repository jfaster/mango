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

package org.jfaster.mango.support;

import org.jfaster.mango.annotation.Sharding;
import org.jfaster.mango.sharding.*;

import java.lang.annotation.Annotation;

/**
 * @author ash
 */
public class MockSharding implements Annotation, Sharding {

  private Class<? extends TableShardingStrategy> tableShardingStrategy = NotUseTableShardingStrategy.class;

  private Class<? extends DatabaseShardingStrategy> databaseShardingStrategy = NotUseDatabaseShardingStrategy.class;

  private Class<? extends ShardingStrategy> shardingStrategy = NotUseShardingStrategy.class;

  public MockSharding() {
  }

  public MockSharding(Class<? extends TableShardingStrategy> tableShardingStrategy, Class<? extends DatabaseShardingStrategy> databaseShardingStrategy, Class<? extends ShardingStrategy> shardingStrategy) {
    this.tableShardingStrategy = tableShardingStrategy;
    this.databaseShardingStrategy = databaseShardingStrategy;
    this.shardingStrategy = shardingStrategy;
  }

  @Override
  public Class<? extends TableShardingStrategy> tableShardingStrategy() {
    return tableShardingStrategy;
  }

  @Override
  public Class<? extends DatabaseShardingStrategy> databaseShardingStrategy() {
    return databaseShardingStrategy;
  }

  @Override
  public Class<? extends ShardingStrategy> shardingStrategy() {
    return shardingStrategy;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    throw new UnsupportedOperationException();
  }

}
