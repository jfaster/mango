/*
 * Copyright 2014 mango.concurrent.cc
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

package cc.concurrent.mango.runtime.operator;

import cc.concurrent.mango.exception.IncorrectParameterCountException;
import cc.concurrent.mango.exception.IncorrectParameterTypeException;
import cc.concurrent.mango.exception.IncorrectSqlException;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.parser.ASTIterableParameter;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.Iterables;
import cc.concurrent.mango.util.TypeToken;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class BatchUpdateOperator extends CacheableOperator {

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
        List<Object[]> batchArgs = new ArrayList<Object[]>(iterables.size());
        List<String> sqls = new ArrayList<String>(iterables.size());
        for (Object obj : iterables) {
            RuntimeContext context = buildRuntimeContext(new Object[]{obj});
            if (keys != null) { // 表示使用cache
                keys.add(getCacheKey(context));
            }
            batchArgs.add(rootNode.getArgs(context));
            sqls.add(rootNode.getSql(context));
        }
        int[] ints = executeDb(sqls, batchArgs);
        if (keys != null) { // 表示使用cache
            deleteFromCache(keys);
        }
        return ints;
    }

    private int[] executeDb(List<String> sqls, List<Object[]> batchArgs) {
        int[] ints = null;
        long now = System.nanoTime();
        try {
            ints = isUniqueSql(sqls) ?
                    jdbcTemplate.batchUpdate(getDataSource(), sqls.get(0), batchArgs) :
                    jdbcTemplate.batchUpdate(getDataSource(), sqls, batchArgs);
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

}
