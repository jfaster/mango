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

import java.lang.reflect.Type;
import java.util.Map;
import java.util.Set;

/**
 * @author ash
 */
public abstract class SimpleCacheHandler implements CacheHandler {

  @Override
  public Object get(String key, Type type, Class<?> daoClass) {
    return get(key, type);
  }

  @Override
  public Map<String, Object> getBulk(Set<String> keys, Type type, Class<?> daoClass) {
    return getBulk(keys, type);
  }

  @Override
  public void set(String key, Object value, int exptimeSeconds, Class<?> daoClass) {
    set(key, value, exptimeSeconds);
  }

  @Override
  public void add(String key, Object value, int exptimeSeconds, Class<?> daoClass) {
    add(key, value, exptimeSeconds);
  }

  @Override
  public void delete(String key, Class<?> daoClass) {
    delete(key);
  }

  @Override
  public void batchDelete(Set<String> keys, Class<?> daoClass) {
    batchDelete(keys);
  }

  public abstract Object get(String key, Type type);

  public abstract Map<String, Object> getBulk(Set<String> keys, Type type);

  public abstract void set(String key, Object value, int exptimeSeconds);

  public abstract void delete(String key);

  public void add(String key, Object value, int exptimeSeconds) {
    throw new UnsupportedOperationException("If @Cache.cacheNullObject is true, please override add method");
  }

  public void batchDelete(Set<String> keys) {
    for (String key : keys) {
      delete(key);
    }
  }

}
