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

package org.jfaster.mango.operator.cache;

import org.jfaster.mango.stat.InvocationStat;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Set;

/**
 * @author ash
 */
public interface CacheBase {

  /**
   * 是否是多个key
   */
  public boolean isUseMultipleKeys();

  /**
   * 是否缓存数据库中的null对象
   */
  public boolean isCacheNullObject();

  /**
   * 是否缓存数据库中的空列表
   */
  public boolean isCacheEmptyList();

  /**
   * 存入缓存
   */
  public void setToCache(String key, Object value, InvocationStat stat);

  /**
   * 存入缓存
   */
  public void addToCache(String key, Object value, InvocationStat stat);

  /**
   * 从缓存删除单个key
   */
  public void deleteFromCache(String key, InvocationStat stat);

  /**
   * 从缓存删除多个key
   */
  public void batchDeleteFromCache(Set<String> keys, InvocationStat stat);

  /**
   * 从缓存读取单个key
   */
  public Object getFromCache(String key, InvocationStat stat);

  /**
   * 从缓存读取多个key
   */
  @Nullable
  public Map<String, Object> getBulkFromCache(Set<String> keys, InvocationStat stat);

  /**
   * 获得缓存过期时间，单位为秒
   */
  public int getExptimeSeconds();

}
