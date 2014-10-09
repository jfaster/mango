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
import org.jfaster.mango.util.reflect.MethodDescriptor;

import javax.sql.DataSource;

/**
 * @author ash
 */
public class UpdateOperator extends AbstractOperator {

    private boolean returnGeneratedId;

    private Class<? extends Number> returnType;

    protected UpdateOperator(ASTRootNode rootNode, MethodDescriptor md) {
        super(rootNode);
        init(md, rootNode.getSQLType());
    }

    private void init(MethodDescriptor md, SQLType sqlType) {
        ReturnGeneratedId returnGeneratedIdAnno = md.getAnnotation(ReturnGeneratedId.class);
        returnGeneratedId = returnGeneratedIdAnno != null // 要求返回自增id
                && sqlType == SQLType.INSERT; // 是插入语句
        if (int.class.equals(md.getRawReturnType())) {
            returnType = int.class;
        } else if (long.class.equals(md.getRawReturnType())) {
            returnType = long.class;
        } else {
            throw new UnreachableCodeException();
        }
    }

    @Override
    public Object execute(Object[] values) {
        InvocationContext context = invocationContextFactory.newInvocationContext(values);
        return execute(context);
    }

    public Number execute(InvocationContext context) {
        context.setGlobalTable(tableGenerator.getTable(context));
        DataSource ds = dataSourceGenerator.getDataSource(context);

        rootNode.render(context);
        PreparedSql preparedSql = context.getPreparedSql();
        invocationInterceptorChain.intercept(preparedSql, context);  // 拦截器

        String sql = preparedSql.getSql();
        Object[] args = preparedSql.getArgs().toArray();
        Number r = executeDb(ds, sql, args);
        return r;
    }

    private Number executeDb(DataSource ds, String sql, Object[] args) {
        Number r = null;
        long now = System.nanoTime();
        try {
            if (returnGeneratedId) {
                GeneratedKeyHolder holder = new GeneratedKeyHolder(returnType);
                jdbcOperations.update(ds, sql, args, holder);
                r = holder.getKey();
            } else {
                r = jdbcOperations.update(ds, sql, args);
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
