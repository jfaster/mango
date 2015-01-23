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
import org.jfaster.mango.annotation.SingleColumnFunctional;
import org.jfaster.mango.exception.IncorrectDefinitionException;
import org.jfaster.mango.invoker.Function;
import org.jfaster.mango.jdbc.JdbcUtils;
import org.jfaster.mango.mapper.BeanPropertyRowMapper;
import org.jfaster.mango.mapper.FunctionalSingleColumnRowMapper;
import org.jfaster.mango.mapper.RowMapper;
import org.jfaster.mango.mapper.SingleColumnRowMapper;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.reflect.MethodDescriptor;
import org.jfaster.mango.reflect.Reflection;
import org.jfaster.mango.reflect.TypeToken;
import org.jfaster.mango.reflect.TypeWrapper;

import javax.sql.DataSource;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

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
        SingleColumnFunctional funcAnno = md.getAnnotation(SingleColumnFunctional.class);
        Mapper mapperAnno = md.getAnnotation(Mapper.class);
        Results resultsAnoo = md.getAnnotation(Results.class);
        checkAnno(funcAnno, mapperAnno, resultsAnoo);
        if (funcAnno == null) {
            TypeWrapper tw = new TypeWrapper(md.getReturnType());
            isForList = tw.isList();
            isForSet = tw.isSet();
            isForArray = tw.isArray();
            mappedClass = tw.getMappedClass();
            rowMapper = getRowMapper(mappedClass, mapperAnno, resultsAnoo);
        } else {
            initFunctionalSingleColumnRowMapper(funcAnno, md.getReturnType());
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

    private <T> RowMapper<?> getRowMapper(Class<T> clazz, Mapper mapperAnno, Results resultsAnoo) {
        if (mapperAnno != null) { // 自定义mapper
            return Reflection.instantiate(mapperAnno.value());
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

    private void checkAnno(SingleColumnFunctional funcAnno, Mapper mapperAnno, Results resultsAnoo) {
        int t = 0;
        if (funcAnno != null) {
            t++;
        }
        if (mapperAnno != null) {
            t++;
        }
        if (resultsAnoo != null) {
            t++;
        }
        if (t > 1) {
            throw new IncorrectDefinitionException(""); // TODO
        }
    }

    private void initFunctionalSingleColumnRowMapper(SingleColumnFunctional funcAnno, Type returnType) {
        Class<? extends Function<?, ?>> funcClass = funcAnno.value();
        Function<?,?> function = Reflection.instantiate(funcClass);
        TypeToken<? extends Function<?, ?>> funcToken = TypeToken.of(funcClass);
        TypeToken<?> inputToken = funcToken.resolveType(Function.class.getTypeParameters()[0]);
        TypeToken<?> outputToken = funcToken.resolveType(Function.class.getTypeParameters()[1]);

        Class<?> fromDbClass = inputToken.getRawType();
        if (!JdbcUtils.isSingleColumnClass(fromDbClass)) {
            throw new RuntimeException(); // TODO
        }

        boolean fetchMultiData = funcAnno.fetchMultiData();
        Type mappedType;
        if (fetchMultiData) {
            TypeWrapper tw = new TypeWrapper(returnType);
            if (!tw.isIterable()) {
                throw new RuntimeException(); // TODO
            }
            isForList = tw.isList();
            isForSet = tw.isSet();
            isForArray = tw.isArray();
            mappedType = tw.getMappedType();
            mappedClass = tw.getMappedClass();
        } else {
            mappedType = returnType;
            mappedClass = TypeToken.of(mappedType).getRawType();
        }

        // 类型检查
        TypeToken<?> wrapMappedToken = TypeToken.of(mappedType).wrap();
        if (function.inverseCheck()) { // 针对继承GenericFunction的
            if (!outputToken.isAssignableFrom(wrapMappedToken)) {
                throw new ClassCastException(); // TODO
            }
        } else { // 针对继承LiteFunction的
            if (!wrapMappedToken.isAssignableFrom(outputToken)) {
                throw new ClassCastException(); // TODO
            }
        }
        rowMapper = getFunctionalSingleColumnRowMapper(function, fromDbClass, mappedClass, mappedType);
    }

    private <T> RowMapper<?> getFunctionalSingleColumnRowMapper(
            Function function, Class<?> fromDbClass, Class<T> mappedClass, Type mappedType) {
        return new FunctionalSingleColumnRowMapper<T>(function, fromDbClass, mappedClass, mappedType);
    }

}
