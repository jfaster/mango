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
import org.jfaster.mango.parser.RuntimeEmptyParameter;
import org.jfaster.mango.reflect.MethodDescriptor;
import org.jfaster.mango.reflect.Reflection;
import org.jfaster.mango.reflect.ReturnDescriptor;
import org.jfaster.mango.util.Strings;

import javax.sql.DataSource;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author ash
 */
public class QueryOperator extends AbstractOperator {

    protected RowMapper<?> rowMapper;
    protected ReturnDescriptor returnDescriptor;
    protected ListSupplier listSupplier;
    protected SetSupplier setSupplier;

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
        } else if (returnDescriptor.isSetAssignable()) {
            setSupplier = new HashSetSupplier();
        }
    }

    @Override
    public Object execute(Object[] values) {
        InvocationContext context = invocationContextFactory.newInvocationContext(values);
        return execute(context);
    }

    protected Object execute(InvocationContext context) {
        context.setGlobalTable(tableGenerator.getTable(context));
        rootNode.render(context);

        if (context.getRuntimeEmptyParameters() != null) {
            if (config.isCompatibleWithEmptyList()) {
                return EmptyObject();
            } else {
                List<String> fullNames = new ArrayList<String>();
                for (RuntimeEmptyParameter rep : context.getRuntimeEmptyParameters()) {
                    fullNames.add(Strings.getFullName(rep.getParameterName(), rep.getPropertyPath()));
                }
                String str = fullNames.size() == 1 ? fullNames.get(0) : fullNames.toString();
                throw new IllegalArgumentException("value of " +
                    str + " can't be empty, error SQL [" + context.getPreparedSql().getSql() + "]");
            }
        }

        PreparedSql preparedSql = context.getPreparedSql();
        invocationInterceptorChain.intercept(preparedSql, context); // 拦截器

        String sql = preparedSql.getSql();
        Object[] args = preparedSql.getArgs().toArray();

        DataSource ds = dataSourceGenerator.getDataSource(context);
        return executeFromDb(ds, sql, args);
    }

    private Object executeFromDb(final DataSource ds, final String sql, final Object[] args) {
        Object r;
        boolean success = false;
        long now = System.nanoTime();
        try {

            r = new QueryVisitor() {

                @Override
                Object visitForList() {
                    return jdbcOperations.queryForList(ds, sql, args, listSupplier, rowMapper);
                }

                @Override
                Object visitForSet() {
                    return jdbcOperations.queryForSet(ds, sql, args, setSupplier, rowMapper);
                }

                @Override
                Object visitForArray() {
                    return jdbcOperations.queryForArray(ds, sql, args, rowMapper);

                }

                @Override
                Object visitForObject() {
                    return jdbcOperations.queryForObject(ds, sql, args, rowMapper);
                }
            }.visit();

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

    private Object EmptyObject() {
        return new QueryVisitor() {
            @Override
            Object visitForList() {
                return listSupplier.get(rowMapper.getMappedClass());
            }

            @Override
            Object visitForSet() {
                return setSupplier.get(rowMapper.getMappedClass());
            }

            @Override
            Object visitForArray() {
                return Array.newInstance(rowMapper.getMappedClass(), 0);
            }

            @Override
            Object visitForObject() {
                return null;
            }
        }.visit();
    }

    abstract class QueryVisitor {

        public Object visit() {
            Object r;
            if (returnDescriptor.isCollection()
                    || returnDescriptor.isListAssignable()) {
                r = visitForList();
            } else if (returnDescriptor.isSetAssignable()) {
                r = visitForSet();
            } else if (returnDescriptor.isArray()) {
                r = visitForArray();
            } else {
                r = visitForObject();
            }
            return r;
        }

        abstract Object visitForList();
        abstract Object visitForSet();
        abstract Object visitForArray();
        abstract Object visitForObject();

    }

}
