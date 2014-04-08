/*
 * Copyright 2014 mango.concurrent.cc
 *
 * The Netty Project licenses this file to you under the Apache License,
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

package cc.concurrent.mango.runtime;

import cc.concurrent.mango.CacheExpire;

/**
 * @author ash
 */
public class CacheDescriptor {

    private boolean useCache; // 是否使用缓存

    private String prefix; // 缓存key前缀

    private CacheExpire expire; // 缓存过期控制

    private int num; // expire的数量

    private String parameterName; // 缓存参数名

    private String propertyPath; // 缓存参数属性路径

    public boolean isUseCache() {
        return useCache;
    }

    public void setUseCache(boolean useCache) {
        this.useCache = useCache;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public int getExpires() {
        return expire.getExpireTime() * num;
    }

    public void setExpire(CacheExpire expire) {
        this.expire = expire;
    }

    public void setNum(int num) {
        this.num = num;
    }

    public String getParameterName() {
        return parameterName;
    }

    public void setParameterName(String parameterName) {
        this.parameterName = parameterName;
    }

    public String getPropertyPath() {
        return propertyPath;
    }

    public void setPropertyPath(String propertyPath) {
        this.propertyPath = propertyPath;
    }

}
