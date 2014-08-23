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

package org.jfaster.mango.runtime.operator;

import org.jfaster.mango.*;
import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.Rename;
import org.jfaster.mango.annotation.ShardBy;
import org.jfaster.mango.datasource.DataSourceFactory;
import org.jfaster.mango.datasource.DataSourceRouter;
import org.jfaster.mango.datasource.IgnoreDataSourceRouter;
import org.jfaster.mango.exception.IncorrectAnnotationException;
import org.jfaster.mango.exception.IncorrectDefinitionException;
import org.jfaster.mango.exception.IncorrectParameterTypeException;
import org.jfaster.mango.jdbc.JdbcTemplate;
import org.jfaster.mango.runtime.*;
import org.jfaster.mango.runtime.parser.ASTRootNode;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.ToStringHelper;
import org.jfaster.mango.util.TypeToken;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;
import org.jfaster.mango.util.reflect.Reflection;

import javax.annotation.Nullable;
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

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(AbstractOperator.class);

    /**
     * 用于对db进行操作
     */
    protected final JdbcTemplate jdbcTemplate = new JdbcTemplate();

    /**
     * 数据源工厂，通过{@link this#setDataSourceFactory(org.jfaster.mango.datasource.DataSourceFactory)}初始化
     */
    private DataSourceFactoryHolder dataSourceFactoryHolder;

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
    private String table;

    /**
     * 分表
     */
    private TablePartition tablePartition;

    /**
     * 数据源路由
     */
    private DataSourceRouter dataSourceRouter;

    /**
     * shardBy参数名
     */
    private String shardParameterName;

    /**
     * shardBy属性路径
     */
    private String shardPropertyPath;


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

    protected AbstractOperator(ASTRootNode rootNode, Method method, SQLType sqlType) {
        this.rootNode = rootNode;
        this.method = method;
        this.sqlType = sqlType;
        init();
        dbInitPostProcessor();
    }

    @Override
    public void setDataSourceFactoryHolder(DataSourceFactoryHolder dataSourceFactoryHolder) {
        this.dataSourceFactoryHolder = dataSourceFactoryHolder;
    }

    @Override
    public void setStatsCounter(StatsCounter statsCounter) {
        this.statsCounter = statsCounter;
    }

    protected RuntimeContext buildRuntimeContext(Object[] methodArgs) {
        Map<String, Object> parameters = new HashMap<String, Object>();
        for (int i = 0; i < methodArgs.length; i++) {
            parameters.put(getParameterNameByIndex(i), methodArgs[i]);
        }
        return new RuntimeContextImpl(parameters);
    }

    protected DataSource getDataSource(RuntimeContext context) {
        return getDataSource(getDataSourceName(context));
    }

    protected DataSource getDataSource(String dsn) {
        final DataSourceFactory dataSourceFactory = dataSourceFactoryHolder.get();
        DataSource ds = dataSourceFactory.getDataSource(dsn, sqlType);
        if (ds == null) {
            throw new IncorrectDefinitionException("can't find datasource for name " + dsn);
        }
        return ds;
    }

    @Nullable
    protected String getDataSourceName(RuntimeContext context) {
        String dsn = dataSourceRouter != null ?
                dataSourceRouter.getDataSourceName(context.getPropertyValue(shardParameterName, shardPropertyPath)) :
                dataSourceName;
        return dsn;
    }

    protected String getParameterNameByIndex(int index) {
        String alias = aliases[index];
        return alias != null ? alias : String.valueOf(index + 1);
    }

    protected TypeContext getTypeContext() {
        if (typeContext == null) {
            typeContext = doGetTypeContext();
        }
        return typeContext;
    }

    private void init() {
        configDb();
        alias();
        shardBy();
        rootNode.init(table, tablePartition, shardParameterName, shardPropertyPath);
        if (logger.isInfoEnabled()) {
            String staticSql = rootNode.getStaticSql();
            if (staticSql != null) {
                logger.info("{} build a static sql \"{}\"", ToStringHelper.toString(method), staticSql);
            } else {
                logger.info("{} can't build static sql", ToStringHelper.toString(method));
            }
        }
        rootNode.checkType(getTypeContext()); // 检测sql中的参数是否和方法上的参数匹配
    }

    /**
     * 构建别名
     */
    private void alias() {
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
    }

    /**
     * 提取{@link org.jfaster.mango.annotation.ShardBy}参数
     */
    private void shardBy() {
        Annotation[][] pass = method.getParameterAnnotations();
        int num = 0;
        for (int i = 0; i < pass.length; i++) {
            Annotation[] pas = pass[i];
            for (Annotation pa : pas) {
                if (ShardBy.class.equals(pa.annotationType())) {
                    shardParameterName = getParameterNameByIndex(i);
                    shardPropertyPath = ((ShardBy) pa).value();
                    num++;
                }
            }
        }
        if (tablePartition != null && num != 1) {
            throw new IncorrectDefinitionException("if @DB.tablePartition is defined, " +
                    "need one and only one @ShardBy on method's parameter");
        }
        if (num == 1) {
            Type shardType = getTypeContext().getPropertyType(shardParameterName, shardPropertyPath);
            TypeToken typeToken = new TypeToken(shardType);
            Class<?> mappedClass = typeToken.getMappedClass();
            if (mappedClass == null || typeToken.isIterable()) {
                throw new IncorrectParameterTypeException("the type of parameter Modified @ShardBy is error, " +
                        "type is " + shardType);
            }
        }
    }

    /**
     * 配置db信息
     */
    private void configDb() {
        DB dbAnno = method.getDeclaringClass().getAnnotation(DB.class);
        if (dbAnno == null) {
            throw new IncorrectAnnotationException("need @DB on dao interface");
        }
        dataSourceName = dbAnno.dataSource();
        if (!Strings.isNullOrEmpty(dbAnno.table())) {
            table = dbAnno.table();
        }
        Class<? extends TablePartition> tpc = dbAnno.tablePartition();
        if (tpc != null && !tpc.equals(IgnoreTablePartition.class)) {
            tablePartition = Reflection.instantiate(tpc);
        }
        Class<? extends DataSourceRouter> dsrc = dbAnno.dataSourceRouter();
        if (dsrc != null && !dsrc.equals(IgnoreDataSourceRouter.class)) {
            dataSourceRouter = Reflection.instantiate(dsrc);
        }

        if (tablePartition != null && table == null) { // 使用了分表但没有使用全局表名则抛出异常
            throw new IncorrectDefinitionException("if @DB.tablePartition is defined, @DB.table must be defined");
        }

        if (dataSourceRouter != null && tablePartition == null) { // 使用了数据源路由但没有使用分表则抛出异常
            throw new IncorrectDefinitionException("if @DB.dataSourceRouter is defined, " +
                    "@DB.tablePartition must be defined");
        }
    }

    /**
     * 构建类型上下文
     *
     * @return
     */
    private TypeContext doGetTypeContext() {
        Type[] methodArgTypes = getMethodArgTypes(method);
        Map<String, Type> parameterTypeMap = new HashMap<String, Type>();
        for (int i = 0; i < methodArgTypes.length; i++) {
            parameterTypeMap.put(getParameterNameByIndex(i), methodArgTypes[i]);
        }
        return new TypeContextImpl(parameterTypeMap);
    }

    protected void dbInitPostProcessor() {
    }

    abstract Type[] getMethodArgTypes(Method method);

}
