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

package org.jfaster.mango.plugin.stats;

import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.operator.OperatorStats;
import org.jfaster.mango.util.ToStringHelper;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class ExtendStats {

    private OperatorStats operatorStats;

    private Method method;

    public ExtendStats(OperatorStats operatorStats) {
        this.operatorStats = operatorStats;
        this.method = operatorStats.getMethod();
    }

    public String getSimpleClassName() {
        return method.getDeclaringClass().getSimpleName();
    }

    public String getSimpleMethodName() {
        return method.getName() + "(" + method.getParameterTypes().length + ")";
    }

    public String getSql() {
        return method.getAnnotation(SQL.class).value();
    }

    public List<String> getStrParameterTypes() {
        List<String> r = new ArrayList<String>();
        for (Type type : method.getGenericParameterTypes()) {
            r.add(ToStringHelper.toString(type));
        }
        return r;
    }

    public String getType() {
        return operatorStats.getType().name().toLowerCase();
    }

}
