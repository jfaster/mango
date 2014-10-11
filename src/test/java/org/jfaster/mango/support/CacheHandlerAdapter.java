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

import org.jfaster.mango.cache.CacheHandler;

import java.util.Map;
import java.util.Set;

/**
 * @author ash
 */
public class CacheHandlerAdapter implements CacheHandler {

    @Override
    public Object get(String key) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Object> getBulk(Set<String> keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void set(String key, Object value, int expires) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(Set<String> keys) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void delete(String key) {
        throw new UnsupportedOperationException();
    }

}
