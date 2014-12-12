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
import org.jfaster.mango.mapper.BeanPropertyRowMapper;
import org.jfaster.mango.jdbc.JdbcUtils;
import org.jfaster.mango.mapper.RowMapper;
import org.jfaster.mango.mapper.SingleColumnRowMapper;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.reflect.MethodDescriptor;
import org.jfaster.mango.reflect.Reflection;
import org.jfaster.mango.reflect.TypeWrapper;

import javax.sql.DataSource;

/**
 * @author ash
 */
public class QueryOperator extends AbstractOperator {

    protected RowMapper<?> rowMapper;
    protected boolean isForList;
    protected boolean isForSet;
    protected boolean isForArray;
    protected Class<?> mappedClass;

    protected QueryOperator(ASTRootNode rootNode, MethodDescriptor md) {
        super(rootNode);
        init(md);
    }

    private void init(MethodDescriptor md) {
        TypeWrapper tw = new TypeWrapper(md.getReturnType());
        isForList = tw.isList();
        isForSet = tw.isSet();
        isForArray = tw.isArray();
        mappedClass = tw.getMappedClass();
        rowMapper = getRowMapper(mappedClass, md);
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
            if (isForList) {
                r = jdbcOperations.queryForList(ds, sql, args, rowMapper);
            } else if (isForSet) {
                r = jdbcOperations.queryForSet(ds, sql, args, rowMapper);
            } else if (isForArray) {
                r= jdbcOperations.queryForArray(ds, sql, args, rowMapper);
            } else {
                r = jdbcOperations.queryForObject(ds, sql, args, rowMapper);
            }
            success = true;
        } finally {
            long cost = System.nanoTime() - now;
            if (success) {
                statsCounter.recordExecuteSuccess(cost);
            } else {
                statsCounter.recordExecuteException(cost);
            }
        }
        return r;
    }

    private static <T> RowMapper<?> getRowMapper(Class<T> clazz, MethodDescriptor md) {
        Mapper mapperAnno = md.getAnnotation(Mapper.class);
        if (mapperAnno != null) { // 自定义mapper
            return Reflection.instantiate(mapperAnno.value());
        }
        return JdbcUtils.isSingleColumnClass(clazz) ?
                new SingleColumnRowMapper<T>(clazz) :
                new BeanPropertyRowMapper<T>(clazz);
    }


}
