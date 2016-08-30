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

import com.alibaba.fastjson.JSON;
import org.jfaster.mango.operator.cache.SimpleCacheHandler;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 使用本地内存实现缓存
 *
 * @author ash
 */
public class MockRedisCacheHandler extends SimpleCacheHandler {

  private final ConcurrentHashMap<String, String> cache = new ConcurrentHashMap<String, String>();

  @Override
  public Object get(String key, Type type) {
    String value = cache.get(key);
    return value != null ? JSON.parseObject(value, type) : null;
  }

  @Override
  public Map<String, Object> getBulk(Set<String> keys, Type type) {
    Map<String, Object> map = new HashMap<String, Object>();
    for (String key : keys) {
      Object value = get(key, type);
      if (value != null) {
        map.put(key, value);
      }
    }
    return map;
  }

  @Override
  public void set(String key, Object value, int exptimeSeconds) {
    cache.put(key, JSON.toJSONString(value));
  }

  @Override
  public void delete(String key) {
    cache.remove(key);
  }

  @Override
  public void add(String key, Object value, int exptimeSeconds) {
    cache.putIfAbsent(key, JSON.toJSONString(value));
  }

}
