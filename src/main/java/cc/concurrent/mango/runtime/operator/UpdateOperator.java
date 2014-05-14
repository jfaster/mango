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

import cc.concurrent.mango.ReturnGeneratedId;
import cc.concurrent.mango.exception.IncorrectSqlException;
import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.parser.ASTIterableParameter;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.Iterables;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class UpdateOperator extends CacheableOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(UpdateOperator.class);

    private boolean returnGeneratedId;

    public UpdateOperator(ASTRootNode rootNode, Method method, SQLType sqlType) {
        super(rootNode, method, sqlType);
        init();
    }

    void init() {
        ReturnGeneratedId returnGeneratedIdAnno = method.getAnnotation(ReturnGeneratedId.class);
        returnGeneratedId = returnGeneratedIdAnno != null // 要求返回自增id
                && sqlType == SQLType.INSERT; // 是插入语句
    }

    @Override
    Type[] getMethodArgTypes(Method method) {
        return method.getGenericParameterTypes();
    }

    @Override
    protected void cacheInitPostProcessor() {
        if (isUseCache()) {
            List<ASTIterableParameter> aips = rootNode.getASTIterableParameters();
            if (aips.size() > 1) {
                throw new IncorrectSqlException("if use cache, sql's in clause expected less than or equal 1 but " +
                        aips.size()); // sql中不能有多个in语句
            }
        }
    }

    @Override
    public Object execute(Object[] methodArgs) {
        RuntimeContext context = buildRuntimeContext(methodArgs);
        ParsedSql parsedSql = rootNode.buildSqlAndArgs(context);
        String sql = parsedSql.getSql();
        Object[] args = parsedSql.getArgs();
        int r = executeDb(sql, args);
        if (isUseCache()) {
            Object obj = getKeySuffixObj(context);
            Iterables iterables = new Iterables(obj);
            if (iterables.isIterable()) { // 多个key，例如：update table set name='ash' where id in (1, 2, 3);
                Set<String> keys = new HashSet<String>(iterables.size() * 2);
                for (Object keySuffix : iterables) {
                    String key = getKey(keySuffix);
                    keys.add(key);
                }
                statsCounter.recordEviction(keys.size());
                deleteFromCache(keys);
                if (logger.isDebugEnabled()) {
                    logger.debug("cache delete #keys={}", keys);
                }
            } else { // 单个key，例如：update table set name='ash' where id ＝ 1;
                String key = getKey(obj);
                statsCounter.recordEviction(1);
                deleteFromCache(key);
                if (logger.isDebugEnabled()) {
                    logger.debug("cache delete #key={}", key);
                }
            }
        }
        return r;
    }

    private int executeDb(String sql, Object[] args) {
        if (logger.isDebugEnabled()) {
            logger.debug("{} #args={}", sql, args);
        }
        int r = -1;
        long now = System.nanoTime();
        try {
            r = jdbcTemplate.update(getDataSource(), sql, args, returnGeneratedId);
        } finally {
            long cost = System.nanoTime() - now;
            if (r > -1) {
                statsCounter.recordExecuteSuccess(cost);
            } else {
                statsCounter.recordExecuteException(cost);
            }
        }
        if (logger.isDebugEnabled()) {
            logger.debug("{} #result={}", sql, r);
        }
        return r;
    }

}
