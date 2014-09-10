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
import org.jfaster.mango.support.RuntimeContext;
import org.jfaster.mango.parser.ASTIterableParameter;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.util.Iterables;
import org.jfaster.mango.support.SQLType;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;
import org.jfaster.mango.util.reflect.TypeToken;

import javax.sql.DataSource;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.*;

/**
 * @author ash
 */
public class BatchUpdateOperator extends CacheableOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(BatchUpdateOperator.class);


    public BatchUpdateOperator(ASTRootNode rootNode, Method method, SQLType sqlType) {
        super(rootNode, method, sqlType);
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
    protected void dbInitPostProcessor() {
        List<ASTIterableParameter> ips = rootNode.getIterableParameters();
        if (ips.size() > 0) {
            throw new IncorrectSqlException("if use batch update, sql's in clause number expected 0 but " +
                    ips.size()); // sql中不能有in语句
        }
    }

    @Override
    public Object execute(Object[] methodArgs) {
        Object methodArg = methodArgs[0];
        if (methodArg == null) {
            throw new NullPointerException("batchUpdate's parameter can't be null");
        }
        Iterables iterables = new Iterables(methodArg);
        if (iterables.isEmpty()) {
            throw new IllegalArgumentException("batchUpdate's parameter can't be empty");
        }

        Set<String> keys = null;
        if (isUseCache()) {
            keys = new HashSet<String>(iterables.size() * 2);
        }

        Map<String, Group> gorupMap = new HashMap<String, Group>();
        for (Object obj : iterables) {
            RuntimeContext context = buildRuntimeContext(new Object[]{obj});
            if (keys != null) { // 表示使用cache
                keys.add(getCacheKey(context));
            }
            String dataSourceName = getDataSourceName(context);

            Group group = gorupMap.get(dataSourceName);
            if (group == null) {
                group = new Group();
                gorupMap.put(dataSourceName, group);
            }
            String sql = rootNode.getSql(context);
            Object[] args = rootNode.getArgs(context);
            group.add(sql, args);
        }
        int[] ints = executeDb(gorupMap);
        if (keys != null) { // 表示使用cache
            deleteFromCache(keys);
            if (logger.isDebugEnabled()) {
                logger.debug("cache delete #keys={}", keys);
            }
        }
        return ints;
    }

    private int[] executeDb(Map<String, Group> gorupMap) {
        int[] ints = null;
        long now = System.nanoTime();
        try {
            for (Map.Entry<String, Group> entry : gorupMap.entrySet()) {
                DataSource ds = getDataSource(entry.getKey());
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

    private boolean isUniqueSql(List<String> sqls) {
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

    private static class Group {
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
