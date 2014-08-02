/*
 * Copyright 2014 mango.concurrent.cc
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

package cc.concurrent.mango.runtime.operator;

import cc.concurrent.mango.SQL;
import cc.concurrent.mango.exception.IncorrectAnnotationException;
import cc.concurrent.mango.exception.IncorrectReturnTypeException;
import cc.concurrent.mango.exception.IncorrectSqlException;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.runtime.parser.Parser;
import cc.concurrent.mango.util.Strings;

import java.lang.reflect.Method;
import java.util.regex.Pattern;

/**
 * Operator工厂
 *
 * @author ash
 */
public class OperatorFactory {

    private final static Pattern INSERT_PATTERN = Pattern.compile("^\\s*INSERT\\s+", Pattern.CASE_INSENSITIVE);
    private final static Pattern DELETE_PATTERN = Pattern.compile("^\\s*DELETE\\s+", Pattern.CASE_INSENSITIVE);
    private final static Pattern UPDATE_PATTERN = Pattern.compile("^\\s*UPDATE\\s+", Pattern.CASE_INSENSITIVE);
    private final static Pattern SELECT_PATTERN = Pattern.compile("^\\s*SELECT\\s+", Pattern.CASE_INSENSITIVE);

    /**
     * 获取Operator
     *
     * @param method
     * @return
     * @throws Exception
     */
    public static CacheableOperator getOperator(Method method) throws Exception {
        SQL sqlAnno = method.getAnnotation(SQL.class);
        if (sqlAnno == null) {
            throw new IncorrectAnnotationException("each method expected one cc.concurrent.mango.SQL annotation " +
                    "but not found");
        }
        String sql = sqlAnno.value();
        if (Strings.isNullOrEmpty(sql)) {
            throw new IncorrectSqlException("sql is null or empty");
        }
        ASTRootNode rootNode = new Parser(sql).parse().reduce();
        SQLType sqlType = getSQLType(sql);

        Class<?> returnType = method.getReturnType();
        if (sqlType == SQLType.SELECT) { // 查
            return new QueryOperator(rootNode, method, sqlType);
        } else if (int.class.equals(returnType) || long.class.equals(returnType)) { // 更新
            return new UpdateOperator(rootNode, method, sqlType);
        } else if (int[].class.equals(returnType)) { // 批量更新
            return new BatchUpdateOperator(rootNode, method, sqlType);
        } else {
            throw new IncorrectReturnTypeException("if sql don't start with select, " +
                    "update return type expected int, " +
                    "batch update return type expected int[], " +
                    "but " + method.getReturnType());
        }
    }

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
