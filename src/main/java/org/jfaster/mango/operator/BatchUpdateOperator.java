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

import org.jfaster.mango.exception.IncorrectParameterCountException;
import org.jfaster.mango.exception.IncorrectParameterTypeException;
import org.jfaster.mango.exception.IncorrectSqlException;
import org.jfaster.mango.parser.ASTJDBCIterableParameter;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.support.SqlDescriptor;
import org.jfaster.mango.util.Iterables;
import org.jfaster.mango.util.reflect.TypeToken;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author ash
 */
public class BatchUpdateOperator extends AbstractOperator {

    protected BatchUpdateOperator(ASTRootNode rootNode, Method method) {
        super(rootNode, method);
        List<ASTJDBCIterableParameter> jips = rootNode.getJDBCIterableParameters();
        if (jips.size() > 0) {
            throw new IncorrectSqlException("if use batch update, sql's in clause number expected 0 but " +
                    jips.size()); // sql中不能有in语句
        }
    }

    @Override
    Type[] getMethodArgTypes(Method method) {
        if (method.getGenericParameterTypes().length != 1) {
            throw new IncorrectParameterCountException("batch update expected one and only one parameter but " +
                    method.getGenericParameterTypes().length); // 批量更新只能有一个参数
        }
        Type type = method.getGenericParameterTypes()[0];
        TypeToken typeToken = new TypeToken(type);
        Class<?> mappedClass = typeToken.getMappedClass();
        if (mappedClass == null || !typeToken.isIterable()) {
            throw new IncorrectParameterTypeException("parameter of batch update " +
                    "expected array or implementations of java.util.List or implementations of java.util.Set " +
                    "but " + type); // 批量更新的参数必须可迭代
        }
        return new Type[]{mappedClass};
    }

    @Override
    public Object execute(Object[] values) {
        Object firstValue = values[0];
        if (firstValue == null) {
            throw new NullPointerException("batchUpdate's parameter can't be null");
        }
        Iterables iterables = new Iterables(firstValue);
        if (iterables.isEmpty()) {
            throw new IllegalArgumentException("batchUpdate's parameter can't be empty");
        }

        Map<DataSource, Group> gorupMap = new HashMap<DataSource, Group>();
        for (Object obj : iterables) {
            RuntimeContext context = buildRuntimeContext(new Object[]{obj});
            group(context, gorupMap);
        }
        int[] ints = executeDb(gorupMap);
        return ints;
    }

    protected void group(RuntimeContext context, Map<DataSource, Group> groupMap) {
        DataSource ds = getDataSource(context);
        Group group = groupMap.get(ds);
        if (group == null) {
            group = new Group();
            groupMap.put(ds, group);
        }
        rootNode.render(context);
        SqlDescriptor sqlDescriptor = context.getSqlDescriptor();

        // 拦截器
        runtimeInterceptorChain.intercept(sqlDescriptor, context);

        String sql = sqlDescriptor.getSql();
        Object[] args = sqlDescriptor.getArgs().toArray();
        group.add(sql, args);
    }

    protected int[] executeDb(Map<DataSource, Group> groupMap) {
        int[] ints = null;
        long now = System.nanoTime();
        try {
            for (Map.Entry<DataSource, Group> entry : groupMap.entrySet()) {
                DataSource ds = entry.getKey();
                List<String> sqls = entry.getValue().getSqls();
                List<Object[]> batchArgs = entry.getValue().getBatchArgs();
                ints = isUniqueSql(sqls) ?
                        jdbcTemplate.batchUpdate(ds, sqls.get(0), batchArgs) :
                        jdbcTemplate.batchUpdate(ds, sqls, batchArgs);
            }
        } finally {
            long cost = System.nanoTime() - now;
            if (ints != null) {
                statsCounter.recordExecuteSuccess(cost);
            } else {
                statsCounter.recordExecuteException(cost);
            }
        }
        return ints;
    }

    protected boolean isUniqueSql(List<String> sqls) {
        String sql = sqls.get(0);
        boolean r = true;
        for (int i = 1; i < sqls.size(); i++) {
            if (!sql.equals(sqls.get(i))) {
                r = false;
                break;
            }
        }
        return r;
    }

    protected static class Group {
        private List<String> sqls = new LinkedList<String>();
        private List<Object[]> batchArgs = new LinkedList<Object[]>();

        public void add(String sql, Object[] args) {
            sqls.add(sql);
            batchArgs.add(args);
        }

        public List<String> getSqls() {
            return sqls;
        }

        public List<Object[]> getBatchArgs() {
            return batchArgs;
        }
    }

}
