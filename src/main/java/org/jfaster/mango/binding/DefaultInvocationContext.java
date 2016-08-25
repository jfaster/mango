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

import org.jfaster.mango.base.Strings;
import org.jfaster.mango.base.sql.PreparedSql;
import org.jfaster.mango.operator.UnreadableParameterException;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author ash
 */
public class DefaultInvocationContext implements InvocationContext {

    private final Map<String, Object> parameterMap = new HashMap<String, Object>();
    private final List<Object> parameterValues = new LinkedList<Object>();
    private final Map<String, Object> cache = new HashMap<String, Object>();

    private final StringBuilder sql = new StringBuilder();
    private final List<Object> args = new LinkedList<Object>();

    private String globalTable;

    private DefaultInvocationContext() {
    }

    public static DefaultInvocationContext create() {
        return new DefaultInvocationContext();
    }

    @Override
    public void addParameter(String parameterName, Object parameterValue) {
        parameterMap.put(parameterName, parameterValue);
        parameterValues.add(parameterValue);
    }

    @Override
    public Object getBindingValue(String parameterName, BindingParameterInvoker invoker) {
        Object value = getNullableBindingValue(parameterName, invoker);
        if (value == null) {
            String fullName = Strings.getFullName(parameterName, invoker.getBindingParameter().getPropertyPath());
            String key = Strings.isEmpty(invoker.getBindingParameter().getPropertyPath()) ? "parameter" : "property";
            throw new NullPointerException(key + " " + fullName + " need a non-null value");
        }
        return value;
    }

    @Override
    @Nullable
    public Object getNullableBindingValue(String parameterName, BindingParameterInvoker invoker) {
        String key = getCacheKey(parameterName, invoker);
        if (cache.containsKey(key)) { // 有可能缓存null对象
            return cache.get(key);
        }
        if (!parameterMap.containsKey(parameterName)) { // ParameterContext进行过检测，理论上这段代码执行不到
            throw new UnreadableParameterException("The parameter ':" + parameterName + "' is not readable");
        }
        Object obj = parameterMap.get(parameterName);
        Object value = invoker.invoke(obj);
        cache.put(key, value);
        return value;
    }

    @Override
    public void setBindingValue(String parameterName, BindingParameterInvoker invoker, Object value) {
        String key = getCacheKey(parameterName, invoker);
        cache.put(key, value);
    }

    @Override
    @Nullable
    public String getGlobalTable() {
        return globalTable;
    }

    @Override
    public void setGlobalTable(String globalTable) {
        this.globalTable = globalTable;
    }

    @Override
    public void writeToSqlBuffer(String str) {
        sql.append(str);
    }

    @Override
    public void appendToArgs(Object obj) {
        args.add(obj);
    }

    @Override
    public PreparedSql getPreparedSql() {
        return new PreparedSql(sql.toString(), args);
    }

    @Override
    public List<Object> getParameterValues() {
        return parameterValues;
    }

    private String getCacheKey(String parameterName, BindingParameterInvoker invokerGroup) {
        return invokerGroup == null ? parameterName : parameterName + "." + invokerGroup.getBindingParameter().getPropertyPath();
    }

}
