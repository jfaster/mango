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

package org.jfaster.mango.operator;

import org.jfaster.mango.util.reflect.Beans;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author ash
 */
public class InvocationContext {

    private final Map<String, Object> parameterMap = new HashMap<String, Object>();
    private final List<Object> parameterValues = new LinkedList<Object>();
    private final Map<String, Object> cache = new HashMap<String, Object>();

    private final StringBuffer sql = new StringBuffer();
    private final List<Object> args = new LinkedList<Object>();

    private String globalTable;

    public void addParameter(String parameterName, Object parameterValue) {
        parameterMap.put(parameterName, parameterValue);
        parameterValues.add(parameterValue);
    }

    public Object getPropertyValue(String parameterName, String propertyPath) {
        String key = getCacheKey(parameterName, propertyPath);
        Object cachedValue = cache.get(key);
        if (cachedValue != null) { // 非null缓存命中，直接返回
            return cachedValue;
        }
        Object object = parameterMap.get(parameterName);
        Object value = !propertyPath.isEmpty() ?
                Beans.getPropertyValue(object, propertyPath, parameterName) :
                object;
        cache.put(key, value);
        return value;
    }

    @Nullable
    public Object getNullablePropertyValue(String parameterName, String propertyPath) {
        String key = getCacheKey(parameterName, propertyPath);
        if (cache.containsKey(key)) { // 有可能缓存null对象
            return cache.get(key);
        }
        Object object = parameterMap.get(parameterName);
        Object value = !propertyPath.isEmpty() ?
                Beans.getNullablePropertyValue(object, propertyPath, parameterName) :
                object;
        cache.put(key, value);
        return value;
    }

    public void setPropertyValue(String parameterName, String propertyPath, Object propertyValue) {
        String key = getCacheKey(parameterName, propertyPath);
        cache.put(key, propertyValue);
    }

    public String getGlobalTable() {
        return globalTable;
    }

    public void setGlobalTable(String globalTable) {
        this.globalTable = globalTable;
    }

    public void writeToSqlBuffer(String str) {
        sql.append(str);
    }

    public void appendToArgs(Object obj) {
        args.add(obj);
    }

    public SqlDescriptor getSqlDescriptor() {
        return new SqlDescriptor(sql.toString(), args);
    }

    public List<Object> getParameterValues() {
        return parameterValues;
    }

    private String getCacheKey(String parameterName, String propertyPath) {
        return propertyPath.isEmpty() ? parameterName : parameterName + "." + propertyPath;
    }

}
