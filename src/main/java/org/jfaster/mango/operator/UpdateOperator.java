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

import org.jfaster.mango.annotation.ReturnGeneratedId;
import org.jfaster.mango.exception.UnreachableCodeException;
import org.jfaster.mango.jdbc.GeneratedKeyHolder;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.util.SQLType;

import javax.sql.DataSource;
import java.lang.reflect.Method;

/**
 * @author ash
 */
public class UpdateOperator extends AbstractOperator {

    private boolean returnGeneratedId;

    private Class<? extends Number> returnType;

    protected UpdateOperator(ASTRootNode rootNode, Method method) {
        super(rootNode);
        init(method, rootNode.getSQLType());
    }

    private void init(Method method, SQLType sqlType) {
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
    public Object execute(Object[] values) {
        RuntimeContext context = runtimeContextFactory.newRuntimeContext(values);
        return execute(context);
    }

    public Number execute(RuntimeContext context) {
        DataSource ds = dataSourceGenerator.getDataSource(context);
        rootNode.render(context);
        SqlDescriptor sqlDescriptor = context.getSqlDescriptor();

        // 拦截器
        runtimeInterceptorChain.intercept(sqlDescriptor, context);

        String sql = sqlDescriptor.getSql();
        Object[] args = sqlDescriptor.getArgs().toArray();
        Number r = executeDb(ds, sql, args);
        return r;
    }

    private Number executeDb(DataSource ds, String sql, Object[] args) {
        Number r = null;
        long now = System.nanoTime();
        try {
            if (returnGeneratedId) {
                GeneratedKeyHolder holder = new GeneratedKeyHolder(returnType);
                jdbcTemplate.update(ds, sql, args, holder);
                r = holder.getKey();
            } else {
                r = jdbcTemplate.update(ds, sql, args);
            }
        } finally {
            long cost = System.nanoTime() - now;
            if (r != null) {
                statsCounter.recordExecuteSuccess(cost);
            } else {
                statsCounter.recordExecuteException(cost);
            }
        }
        return r;
    }

}
