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

import org.jfaster.mango.operator.RuntimeContext;

import java.util.Map;
import java.util.Set;

/**
 * @author ash
 */
public interface CacheDriver {

    public boolean isUseMultipleKeys();

    public void setToCache(String key, Object value);

    public void deleteFromCache(String key);

    public void deleteFromCache(Set<String> keys);

    public Object getFromCache(String key);

    public Map<String, Object> getBulkFromCache(Set<String> keys);

    public Class<?> getSuffixClass();

    public String getCacheKey(RuntimeContext context);

    public String getCacheKey(Object suffix);

    public Set<String> getCacheKeys(RuntimeContext context);

    public Object getSuffixObj(RuntimeContext context);

    public void setSuffixObj(RuntimeContext context, Object obj);

    public String getInterableProperty();

}
