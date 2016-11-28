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

import org.jfaster.mango.util.Ticker;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 使用本地内存实现缓存
 *
 * @author ash
 */
public class LocalCacheHandler extends SimpleCacheHandler {

  private final ConcurrentHashMap<String, Entry> cache = new ConcurrentHashMap<String, Entry>();

  private final Ticker ticker;

  public LocalCacheHandler() {
    this(Ticker.systemTicker());
  }

  public LocalCacheHandler(Ticker ticker) {
    this.ticker = ticker;
  }

  public Object get(String key) {
    return get(key, null);
  }

  public Map<String, Object> getBulk(Set<String> keys) {
    return getBulk(keys, null);
  }

  @Override
  public Object get(String key, Type type) {
    Entry entry = cache.get(key);
    if (entry == null) {
      return null;
    }
    long now = ticker.read();
    if (entry.getExpireTime() >= now) { // 没有过期
      return entry.getValue();
    } else {
      cache.remove(key, entry);
      return null;
    }
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
    long now = ticker.read();
    Entry entry = new Entry(value, now + TimeUnit.SECONDS.toNanos(exptimeSeconds));
    cache.put(key, entry);
  }

  @Override
  public void delete(String key) {
    cache.remove(key);
  }

  @Override
  public void add(String key, Object value, int exptimeSeconds) {
    long now = ticker.read();
    Entry entry = new Entry(value, now + TimeUnit.SECONDS.toNanos(exptimeSeconds));
    cache.putIfAbsent(key, entry);
  }

  private static class Entry {

    private Object value;

    private long expireTime;

    public Entry(Object value, long expireTime) {
      this.value = value;
      this.expireTime = expireTime;
    }

    public Object getValue() {
      return value;
    }

    public long getExpireTime() {
      return expireTime;
    }

  }

}
