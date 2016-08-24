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

package org.jfaster.mango.binding;

import org.jfaster.mango.invoker.GetterInvokerGroup;
import org.jfaster.mango.base.sql.PreparedSql;
import org.jfaster.mango.operator.UnreadableParameterException;
import org.jfaster.mango.parser.RuntimeEmptyParameter;
import org.jfaster.mango.base.Strings;

import javax.annotation.Nullable;
import java.util.*;

/**
 * @author ash
 */
public class InvocationContext {

    private final Map<String, Object> parameterMap = new HashMap<String, Object>();
    private final List<Object> parameterValues = new LinkedList<Object>();
    private final Map<String, Object> cache = new HashMap<String, Object>();

    private final StringBuilder sql = new StringBuilder();
    private final List<Object> args = new LinkedList<Object>();

    private String globalTable;

    private List<RuntimeEmptyParameter> runtimeEmptyParameters;

    public void addParameter(String parameterName, Object parameterValue) {
        parameterMap.put(parameterName, parameterValue);
        parameterValues.add(parameterValue);
    }

    public Object getPropertyValue(String parameterName, GetterInvokerGroup invokerGroup) {
        Object value = getNullablePropertyValue(parameterName, invokerGroup);
        if (value == null) {
            String fullName = Strings.getFullName(parameterName, invokerGroup.getPropertyPath());
            String key = Strings.isEmpty(invokerGroup.getPropertyPath()) ? "parameter" : "property";
            throw new NullPointerException(key + " " + fullName + " need a non-null value");
        }
        return value;
    }

    @Nullable
    public Object getNullablePropertyValue(String parameterName, GetterInvokerGroup invokerGroup) {
        String key = getCacheKey(parameterName, invokerGroup);
        if (cache.containsKey(key)) { // 有可能缓存null对象
            return cache.get(key);
        }
        if (!parameterMap.containsKey(parameterName)) { // ParameterContext进行过检测，理论上这段代码执行不到
            throw new UnreadableParameterException("The parameter ':" + parameterName + "' is not readable");
        }
        Object obj = parameterMap.get(parameterName);
        Object value = invokerGroup.invoke(obj);
        cache.put(key, value);
        return value;
    }

    public void setPropertyValue(String parameterName, GetterInvokerGroup invokerGroup, Object propertyValue) {
        String key = getCacheKey(parameterName, invokerGroup);
        cache.put(key, propertyValue);
    }

    @Nullable
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

    public PreparedSql getPreparedSql() {
        return new PreparedSql(sql.toString(), args);
    }

    public List<Object> getParameterValues() {
        return parameterValues;
    }

    public void addRuntimeEmptyParameter(RuntimeEmptyParameter rep) {
        if (runtimeEmptyParameters == null) {
            runtimeEmptyParameters = new ArrayList<RuntimeEmptyParameter>();
        }
        runtimeEmptyParameters.add(rep);
    }

    @Nullable
    public List<RuntimeEmptyParameter> getRuntimeEmptyParameters() {
        return runtimeEmptyParameters;
    }

    private String getCacheKey(String parameterName, GetterInvokerGroup invokerGroup) {
        return invokerGroup == null ? parameterName : parameterName + "." + invokerGroup.getPropertyPath();
    }

}
