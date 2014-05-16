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

package cc.concurrent.mango.runtime.operator;

import cc.concurrent.mango.exception.IncorrectParameterCountException;
import cc.concurrent.mango.exception.IncorrectParameterTypeException;
import cc.concurrent.mango.exception.IncorrectSqlException;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.parser.ASTIterableParameter;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.Iterables;
import cc.concurrent.mango.util.TypeToken;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;

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
        List<Object[]> batchArgs = new ArrayList<Object[]>(iterables.size());
        String sql = null;
        for (Object obj : iterables) {
            RuntimeContext context = buildRuntimeContext(new Object[]{obj});
            if (keys != null) { // 表示使用cache
                keys.add(getCacheKey(context));
            }
            if (sql == null) {
                sql = rootNode.getSql(context);
            }
            batchArgs.add(rootNode.getArgs(context));
        }
        int[] ints = executeDb(sql, batchArgs);
        if (keys != null) { // 表示使用cache
            deleteFromCache(keys);
        }
        return ints;
    }

    private int[] executeDb(String sql, List<Object[]> batchArgs) {
        if (logger.isDebugEnabled()) {
            List<String> str = new ArrayList<String>();
            for (Object[] args : batchArgs) {
                str.add(Arrays.toString(args));
            }
            List<List<Object>> debugBatchArgs = new ArrayList<List<Object>>(batchArgs.size());
            for (Object[] batchArg : batchArgs) {
                debugBatchArgs.add(Arrays.asList(batchArg));
            }
            logger.debug("{} #args={}", sql, debugBatchArgs);
        }
        int[] ints = null;
        long now = System.nanoTime();
        try {
            ints = jdbcTemplate.batchUpdate(getDataSource(), sql, batchArgs);
        } finally {
            long cost = System.nanoTime() - now;
            if (ints != null) {
                statsCounter.recordExecuteSuccess(cost);
            } else {
                statsCounter.recordExecuteException(cost);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{} #result={}", sql, ints);
        }
        return ints;
    }

}
