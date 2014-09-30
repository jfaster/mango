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
import org.jfaster.mango.datasource.DataSourceFactoryHolder;
import org.jfaster.mango.exception.IncorrectAnnotationException;
import org.jfaster.mango.exception.IncorrectReturnTypeException;
import org.jfaster.mango.exception.IncorrectSqlException;
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

    private final DataSourceFactoryHolder dataSourceFactoryHolder;
    private final CacheHandler cacheHandler;
    private final InterceptorChain queryInterceptorChain;
    private final InterceptorChain updateInterceptorChain;

    public OperatorFactoryImpl(DataSourceFactoryHolder dataSourceFactoryHolder, CacheHandler cacheHandler,
                               InterceptorChain queryInterceptorChain, InterceptorChain updateInterceptorChain) {
        this.dataSourceFactoryHolder = dataSourceFactoryHolder;
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
        OperatorType operatorType;
        Class<?> returnType = method.getReturnType();
        if (rootNode.getSQLType() == SQLType.SELECT) { // 查
            operatorType = OperatorType.SELECT;
        } else if (int.class.equals(returnType) || long.class.equals(returnType)) { // 更新
            operatorType = OperatorType.UPDATE;
        } else if (int[].class.equals(returnType)) { // 批量更新
            operatorType = OperatorType.BATCHUPDATE;
        } else {
            throw new IncorrectReturnTypeException("if sql don't start with select, " +
                    "update return type expected int, " +
                    "batch update return type expected int[], " +
                    "but " + method.getReturnType());
        }

        Class<?> daoClass = method.getDeclaringClass();
        CacheIgnored cacheIgnoredAnno = method.getAnnotation(CacheIgnored.class);
        Cache cacheAnno = daoClass.getAnnotation(Cache.class);
        boolean useCache = cacheAnno != null && cacheIgnoredAnno == null;

        Operator operator;
        RuntimeInterceptorChain chain;
        if (!useCache) {
            OperatorDriver driver = new OperatorDriverImpl(dataSourceFactoryHolder, operatorType, method, rootNode);
            switch (operatorType) {
                case SELECT:
                    operator = new QueryOperator(rootNode, driver, method);
                    chain = new RuntimeInterceptorChain(queryInterceptorChain, method);
                    break;
                case UPDATE:
                    operator = new UpdateOperator(rootNode, driver, method);
                    chain = new RuntimeInterceptorChain(updateInterceptorChain, method);
                    break;
                default:
                    operator = new BatchUpdateOperator(rootNode, driver);
                    chain = new RuntimeInterceptorChain(updateInterceptorChain, method);
            }
        } else {
            CacheableOperatorDriver driver = new CacheableOperatorDriverImpl(dataSourceFactoryHolder,
                    operatorType, method, rootNode, cacheHandler);
            switch (operatorType) {
                case SELECT:
                    operator = new CacheableQueryOperator(rootNode, driver, method);
                    chain = new RuntimeInterceptorChain(queryInterceptorChain, method);
                    break;
                case UPDATE:
                    operator = new CacheableUpdateOperator(rootNode, driver, method);
                    chain = new RuntimeInterceptorChain(updateInterceptorChain, method);
                    break;
                default:
                    operator = new CacheableBatchUpdateOperator(rootNode, driver);
                    chain = new RuntimeInterceptorChain(updateInterceptorChain, method);
            }
        }
        operator.setRuntimeInterceptorChain(chain);
        operator.setStatsCounter(statsCounter);
        return operator;
    }

}
