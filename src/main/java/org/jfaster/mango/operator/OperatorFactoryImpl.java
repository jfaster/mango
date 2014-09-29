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
import org.jfaster.mango.operator.driver.CacheableOperatorDriver;
import org.jfaster.mango.operator.driver.CacheableOperatorDriverImpl;
import org.jfaster.mango.operator.driver.OperatorDriver;
import org.jfaster.mango.operator.driver.OperatorDriverImpl;
import org.jfaster.mango.parser.Parser;
import org.jfaster.mango.parser.node.ASTRootNode;
import org.jfaster.mango.support.SQLType;
import org.jfaster.mango.util.Strings;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * Operator工厂实现
 *
 * @author ash
 */
public class OperatorFactoryImpl implements OperatorFactory {

    @Override
    public Operator getOperator(Method method, DataSourceFactoryHolder dataSourceFactoryHolder,
                                CacheHandler cacheHandler, StatsCounter statsCounter) throws Exception {
        SQL sqlAnno = method.getAnnotation(SQL.class);
        if (sqlAnno == null) {
            throw new IncorrectAnnotationException("each method expected one @SQL annotation " +
                    "but not found");
        }
        String sql = sqlAnno.value();
        if (Strings.isNullOrEmpty(sql)) {
            throw new IncorrectSqlException("sql is null or empty");
        }
        SQLType sqlType = getSQLType(sql);
        OperatorType operatorType;
        Class<?> returnType = method.getReturnType();
        if (sqlType == SQLType.SELECT) { // 查
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

        ASTRootNode rootNode = new Parser(sql).parse().init();

        Class<?> daoClass = method.getDeclaringClass();
        CacheIgnored cacheIgnoredAnno = method.getAnnotation(CacheIgnored.class);
        Cache cacheAnno = daoClass.getAnnotation(Cache.class);
        boolean useCache = cacheAnno != null && cacheIgnoredAnno == null;

        Operator operator;
        if (!useCache) {
            OperatorDriver driver = new OperatorDriverImpl(dataSourceFactoryHolder, sqlType, operatorType, method, rootNode);
            switch (operatorType) {
                case SELECT:
                    operator = new QueryOperator(rootNode, driver, method, statsCounter);
                    break;
                case UPDATE:
                    operator = new UpdateOperator(rootNode, driver, method, sqlType, statsCounter);
                    break;
                default:
                    operator = new BatchUpdateOperator(rootNode, driver, statsCounter);
            }
        } else {
            CacheableOperatorDriver driver = new CacheableOperatorDriverImpl(dataSourceFactoryHolder, sqlType, operatorType, method, rootNode, cacheHandler);
            switch (operatorType) {
                case SELECT:
                    operator = new CacheableQueryOperator(rootNode, driver, method, statsCounter);
                    break;
                case UPDATE:
                    operator = new CacheableUpdateOperator(rootNode, driver, method, sqlType, statsCounter);
                    break;
                default:
                    operator = new CacheableBatchUpdateOperator(rootNode, driver, statsCounter);
            }
        }
        return operator;
    }

    private final static Pattern INSERT_PATTERN = Pattern.compile("^\\s*INSERT\\s+", Pattern.CASE_INSENSITIVE);
    private final static Pattern DELETE_PATTERN = Pattern.compile("^\\s*DELETE\\s+", Pattern.CASE_INSENSITIVE);
    private final static Pattern UPDATE_PATTERN = Pattern.compile("^\\s*UPDATE\\s+", Pattern.CASE_INSENSITIVE);
    private final static Pattern SELECT_PATTERN = Pattern.compile("^\\s*SELECT\\s+", Pattern.CASE_INSENSITIVE);

    private static SQLType getSQLType(String sql) {
        if (INSERT_PATTERN.matcher(sql).find()) {
            return SQLType.INSERT;
        } else if (DELETE_PATTERN.matcher(sql).find()) {
            return SQLType.DELETE;
        } else if (UPDATE_PATTERN.matcher(sql).find()) {
            return SQLType.UPDATE;
        } else if (SELECT_PATTERN.matcher(sql).find()) {
            return SQLType.SELECT;
        } else {
            throw new IncorrectSqlException("sql must start with INSERT or DELETE or UPDATE or SELECT");
        }
    }
}
