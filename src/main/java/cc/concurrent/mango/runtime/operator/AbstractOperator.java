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

import cc.concurrent.mango.DB;
import cc.concurrent.mango.DataSourceFactory;
import cc.concurrent.mango.Rename;
import cc.concurrent.mango.jdbc.JdbcTemplate;
import cc.concurrent.mango.runtime.*;
import cc.concurrent.mango.runtime.parser.ASTRootNode;
import cc.concurrent.mango.util.Strings;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * 抽象db操作
 *
 * @author ash
 */
public abstract class AbstractOperator implements Operator {

    /**
     * 用于对db进行操作
     */
    protected final JdbcTemplate jdbcTemplate = new JdbcTemplate();

    /**
     * 数据源工厂，通过{@link this#setDataSourceFactory(cc.concurrent.mango.DataSourceFactory)}初始化
     */
    private DataSourceFactory dataSourceFactory;

    /**
     * 统计信息，通过{@link this#setStatsCounter(StatsCounter)}初始化
     */
    protected StatsCounter statsCounter;

    /**
     * 数据源名称
     */
    private String dataSourceName;

    /**
     * 全局表名称
     */
    private String tableName;

    /**
     * 根节点信息
     */
    protected ASTRootNode rootNode;

    /**
     * 方法信息
     */
    protected Method method;

    /**
     * sql类型，对应着增删改查
     */
    protected SQLType sqlType;

    /**
     * 类型上下文
     */
    private TypeContext typeContext;

    /**
     * 变量别名
     */
    private String[] aliases;

    private final static String TABLE = "table";

    protected AbstractOperator(ASTRootNode rootNode, Method method, SQLType sqlType) {
        this.rootNode = rootNode;
        this.method = method;
        this.sqlType = sqlType;
        init();
        dbInitPostProcessor();
    }

    @Override
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public void setStatsCounter(StatsCounter statsCounter) {
        this.statsCounter = statsCounter;
    }

    protected RuntimeContext buildRuntimeContext(Object[] methodArgs) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        if (!Strings.isNullOrEmpty(tableName)) { // 在@DB中设置过全局表名
            parameters.put(TABLE, tableName);
        }
        for (int i = 0; i < methodArgs.length; i++) {
            parameters.put(getParameterNameByIndex(i), methodArgs[i]);
        }
        return new RuntimeContextImpl(parameters);
    }

    protected DataSource getDataSource() {
        return dataSourceFactory.getDataSource(dataSourceName, sqlType);
    }

    protected String getParameterNameByIndex(int index) {
        String alias = aliases[index];
        return alias != null ? alias : String.valueOf(index + 1);
    }

    protected TypeContext getTypeContext() {
        return typeContext;
    }

    private void init() {
        // 构建别名
        Annotation[][] pass = method.getParameterAnnotations();
        aliases = new String[pass.length];
        for (int i = 0; i < pass.length; i++) {
            Annotation[] pas = pass[i];
            for (Annotation pa : pas) {
                if (Rename.class.equals(pa.annotationType())) {
                    aliases[i] = ((Rename) pa).value();
                }
            }
        }

        // 数据源与表信息
        DB dbAnno = method.getDeclaringClass().getAnnotation(DB.class);
        if (dbAnno != null) {
            dataSourceName = dbAnno.dataSource();
            tableName = dbAnno.table();
        }

        // 类型上下文
        typeContext = buildTypeContext(method);

        // 检测sql中的参数是否和方法上的参数匹配
        rootNode.checkType(typeContext);
    }

    private TypeContext buildTypeContext(Method method) {
        Type[] methodArgTypes = getMethodArgTypes(method);
        Map<String, Type> parameterTypeMap = new HashMap<String, Type>();
        if (!Strings.isNullOrEmpty(tableName)) { // 在@DB中设置过全局表名
            parameterTypeMap.put(TABLE, String.class);
        }
        for (int i = 0; i < methodArgTypes.length; i++) {
            parameterTypeMap.put(getParameterNameByIndex(i), methodArgTypes[i]);
        }
        return new TypeContextImpl(parameterTypeMap);
    }

    protected void dbInitPostProcessor() {
    }

    abstract Type[] getMethodArgTypes(Method method);

}
