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

import org.jfaster.mango.operator.cache.CacheHandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
* @author ash
*/
public class CacheHandlerImpl implements CacheHandler {

    private ConcurrentHashMap<String, Object> cache = new ConcurrentHashMap<String, Object>();

    @Override
    public Object get(String key) {
        return cache.get(key);
    }

    @Override
    public Map<String, Object> getBulk(Set<String> keys) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (String key : keys) {
            map.put(key, cache.get(key));
        }
        return map;
    }

    @Override
    public void set(String key, Object value, int expires) {
        cache.put(key, value);
    }

    @Override
    public void add(String key, Object value, int expires) {
        cache.putIfAbsent(key, value);
    }

    @Override
    public void batchDelete(Set<String> keys) {
        for (String key : keys) {
            delete(key);
        }
    }

    @Override
    public void delete(String key) {
        cache.remove(key);
    }
}
