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
import cc.concurrent.mango.exception.UnreachableCodeException;
import cc.concurrent.mango.jdbc.GeneratedKeyHolder;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.parser.ASTIterableParameter;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.logging.InternalLogger;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;

import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Set;

/**
 * @author ash
 */
public class UpdateOperator extends CacheableOperator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(UpdateOperator.class);

    private boolean returnGeneratedId;

    private Class<? extends Number> returnType;

    public UpdateOperator(ASTRootNode rootNode, Method method, SQLType sqlType) {
        super(rootNode, method, sqlType);
        init();
    }

    void init() {
        ReturnGeneratedId returnGeneratedIdAnno = method.getAnnotation(ReturnGeneratedId.class);
        returnGeneratedId = returnGeneratedIdAnno != null // 要求返回自增id
                && sqlType == SQLType.INSERT; // 是插入语句
        if (int.class.equals(method.getReturnType())) {
            returnType = int.class;
        } else if (long.class.equals(method.getReturnType())) {
            returnType = long.class;
        } else {
            throw new UnreachableCodeException();
        }
    }

    @Override
    Type[] getMethodArgTypes(Method method) {
        return method.getGenericParameterTypes();
    }

    @Override
    protected void cacheInitPostProcessor() {
        if (isUseCache()) {
            List<ASTIterableParameter> ips = rootNode.getIterableParameters();
            if (ips.size() > 1) {
                throw new IncorrectSqlException("if use cache, sql's in clause expected less than or equal 1 but " +
                        ips.size()); // sql中不能有多个in语句
            }
        }
    }

    @Override
    public Object execute(Object[] methodArgs) {
        RuntimeContext context = buildRuntimeContext(methodArgs);
        String sql = rootNode.getSql(context);
        Object[] args = rootNode.getArgs(context);
        Number r = executeDb(sql, args);
        if (isUseCache()) { // 如果使用cache，更新后需要从cache中删除对应的key或keys
            if (isUseMultipleKeys()) { // 多个key，例如：update table set name='ash' where id in (1, 2, 3);
                Set<String> keys = getCacheKeys(context);
                deleteFromCache(keys);
                if (logger.isDebugEnabled()) {
                    logger.debug("cache delete #keys={}", keys);
                }
            } else { // 单个key，例如：update table set name='ash' where id ＝ 1;
                String key = getCacheKey(context);
                deleteFromCache(key);
                if (logger.isDebugEnabled()) {
                    logger.debug("cache delete #key={}", key);
                }
            }
        }
        return r;
    }

    private Number executeDb(String sql, Object[] args) {
        if (logger.isDebugEnabled()) {
            logger.debug("{} #args={}", sql, args);
        }
        Number r = null;
        long now = System.nanoTime();
        try {
            if (returnGeneratedId) {
                GeneratedKeyHolder holder = new GeneratedKeyHolder(returnType);
                jdbcTemplate.update(getDataSource(), sql, args, holder);
                r = holder.getKey();
            } else {
                r = jdbcTemplate.update(getDataSource(), sql, args);
            }
        } finally {
            long cost = System.nanoTime() - now;
            if (r != null) {
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
