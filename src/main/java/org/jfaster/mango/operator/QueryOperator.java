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

import org.jfaster.mango.annotation.Mapper;
import org.jfaster.mango.annotation.Result;
import org.jfaster.mango.annotation.Results;
import org.jfaster.mango.jdbc.*;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.reflect.MethodDescriptor;
import org.jfaster.mango.reflect.Reflection;
import org.jfaster.mango.reflect.ReturnDescriptor;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ash
 */
public class QueryOperator extends AbstractOperator {

    protected RowMapper<?> rowMapper;
    protected ReturnDescriptor returnDescriptor;
    protected ListSupplier listSupplier;

    protected QueryOperator(ASTRootNode rootNode, MethodDescriptor md) {
        super(rootNode);
        init(md);
    }

    private void init(MethodDescriptor md) {
        returnDescriptor = md.getReturnDescriptor();
        rowMapper = getRowMapper(returnDescriptor.getMappedClass(), returnDescriptor);
        if (returnDescriptor.isCollection()
                || returnDescriptor.isList()
                || returnDescriptor.isLinkedList()) {
            listSupplier = new LinkedListSuppliter();
        } else if (returnDescriptor.isArrayList()) {
            listSupplier = new ArrayListSuppliter();
        }
    }

    @Override
    public Object execute(Object[] values) {
        InvocationContext context = invocationContextFactory.newInvocationContext(values);
        return execute(context);
    }

    protected Object execute(InvocationContext context) {
        context.setGlobalTable(tableGenerator.getTable(context));
        DataSource ds = dataSourceGenerator.getDataSource(context);

        rootNode.render(context);
        PreparedSql preparedSql = context.getPreparedSql();
        invocationInterceptorChain.intercept(preparedSql, context); // 拦截器

        String sql = preparedSql.getSql();
        Object[] args = preparedSql.getArgs().toArray();
        return executeFromDb(ds, sql, args);
    }

    private Object executeFromDb(DataSource ds, String sql, Object[] args) {
        Object r;
        boolean success = false;
        long now = System.nanoTime();
        try {
            if (returnDescriptor.isCollection()
                    || returnDescriptor.isListAssignable()) {

                r = jdbcOperations.queryForList(ds, sql, args, listSupplier, rowMapper);

            } else if (returnDescriptor.isSetAssignable()) {

                r = jdbcOperations.queryForSet(ds, sql, args, rowMapper);

            } else if (returnDescriptor.isArray()) {

                r = jdbcOperations.queryForArray(ds, sql, args, rowMapper);

            } else {

                r = jdbcOperations.queryForObject(ds, sql, args, rowMapper);

            }
            success = true;
        } finally {
            long cost = System.nanoTime() - now;
            if (success) {
                statsCounter.recordDatabaseExecuteSuccess(cost);
            } else {
                statsCounter.recordDatabaseExecuteException(cost);
            }
        }
        return r;
    }

    private <T> RowMapper<?> getRowMapper(Class<T> clazz, ReturnDescriptor rd) {
        Mapper mapperAnno = rd.getAnnotation(Mapper.class);
        Results resultsAnoo = rd.getAnnotation(Results.class);
        if (mapperAnno != null) { // 自定义mapper
            return Reflection.instantiateClass(mapperAnno.value());
        }
        if (JdbcUtils.isSingleColumnClass(clazz)) { // 单列mapper
            return new SingleColumnRowMapper<T>(clazz);
        }

        // 类属性mapper
        Map<String, String> ptc = new HashMap<String, String>();
        if (resultsAnoo != null) {
            Result[] resultAnnos = resultsAnoo.value();
            if (resultAnnos != null) {
                for (Result resultAnno : resultAnnos) {
                    ptc.put(resultAnno.property().toLowerCase().trim(),
                            resultAnno.column().toLowerCase().trim());
                }
            }
        }
        return new BeanPropertyRowMapper<T>(clazz, ptc);
    }

}
