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

import org.jfaster.mango.annotation.Cache;
import org.jfaster.mango.annotation.CacheIgnored;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.cache.CacheHandler;
import org.jfaster.mango.datasource.factory.DataSourceFactory;
import org.jfaster.mango.exception.IncorrectAnnotationException;
import org.jfaster.mango.exception.IncorrectReturnTypeException;
import org.jfaster.mango.exception.IncorrectSqlException;
import org.jfaster.mango.jdbc.JdbcTemplate;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.parser.Parser;
import org.jfaster.mango.support.SQLType;
import org.jfaster.mango.util.Strings;

import java.lang.reflect.Method;

/**
 * Operator工厂实现
 *
 * @author ash
 */
public class OperatorFactoryImpl implements OperatorFactory {

    private final DataSourceFactory dataSourceFactory;
    private final CacheHandler cacheHandler;
    private final InterceptorChain queryInterceptorChain;
    private final InterceptorChain updateInterceptorChain;

    public OperatorFactoryImpl(DataSourceFactory dataSourceFactory, CacheHandler cacheHandler,
                               InterceptorChain queryInterceptorChain, InterceptorChain updateInterceptorChain) {
        this.dataSourceFactory = dataSourceFactory;
        this.cacheHandler = cacheHandler;
        this.queryInterceptorChain = queryInterceptorChain;
        this.updateInterceptorChain = updateInterceptorChain;
    }

    @Override
    public Operator getOperator(Method method, StatsCounter statsCounter) throws Exception {
        SQL sqlAnno = method.getAnnotation(SQL.class);
        if (sqlAnno == null) {
            throw new IncorrectAnnotationException("each method expected one @SQL annotation " +
                    "but not found");
        }
        String sql = sqlAnno.value();
        if (Strings.isNullOrEmpty(sql)) {
            throw new IncorrectSqlException("sql is null or empty");
        }
        ASTRootNode rootNode = new Parser(sql.trim()).parse().init();

        Class<?> daoClass = method.getDeclaringClass();
        CacheIgnored cacheIgnoredAnno = method.getAnnotation(CacheIgnored.class);
        Cache cacheAnno = daoClass.getAnnotation(Cache.class);
        boolean useCache = cacheAnno != null && cacheIgnoredAnno == null;

        Operator operator;
        RuntimeInterceptorChain chain;
        Class<?> returnType = method.getReturnType();
        if (rootNode.getSQLType() == SQLType.SELECT) { // 查
            operator = useCache ?
                    new CacheableQueryOperator(rootNode, method, cacheHandler) :
                    new QueryOperator(rootNode, method);
            chain = new RuntimeInterceptorChain(queryInterceptorChain, method);
        } else if (int.class.equals(returnType) || long.class.equals(returnType)) { // 更新
            operator = useCache ?
                    new CacheableUpdateOperator(rootNode, method, cacheHandler) :
                    new UpdateOperator(rootNode, method);
            chain = new RuntimeInterceptorChain(updateInterceptorChain, method);
        } else if (int[].class.equals(returnType)) { // 批量更新
            operator = useCache ?
                    new CacheableBatchUpdateOperator(rootNode, method, cacheHandler) :
                    new BatchUpdateOperator(rootNode, method);
            chain = new RuntimeInterceptorChain(updateInterceptorChain, method);
        } else {
            throw new IncorrectReturnTypeException("if sql don't start with select, " +
                    "update return type expected int, " +
                    "batch update return type expected int[], " +
                    "but " + method.getReturnType());
        }

        operator.setJdbcTemplate(new JdbcTemplate());
        operator.setDataSourceFactory(dataSourceFactory);
        operator.setRuntimeInterceptorChain(chain);
        operator.setStatsCounter(statsCounter);
        return operator;
    }

}
