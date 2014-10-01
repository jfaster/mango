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

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.ShardBy;
import org.jfaster.mango.datasource.factory.DataSourceFactory;
import org.jfaster.mango.datasource.router.DataSourceRouter;
import org.jfaster.mango.datasource.router.IgnoreDataSourceRouter;
import org.jfaster.mango.exception.IncorrectAnnotationException;
import org.jfaster.mango.exception.IncorrectDefinitionException;
import org.jfaster.mango.exception.IncorrectParameterTypeException;
import org.jfaster.mango.jdbc.JdbcTemplate;
import org.jfaster.mango.operator.interceptor.RuntimeInterceptorChain;
import org.jfaster.mango.operator.stats.StatsCounter;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.partition.IgnoreTablePartition;
import org.jfaster.mango.partition.TablePartition;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.logging.InternalLogger;
import org.jfaster.mango.util.logging.InternalLoggerFactory;
import org.jfaster.mango.util.reflect.Reflection;
import org.jfaster.mango.util.reflect.TypeToken;

import javax.annotation.Nullable;
import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ash
 */
public abstract class AbstractOperator implements Operator {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(AbstractOperator.class);

    /**
     * 渲染sql的树节点
     */
    protected final ASTRootNode rootNode;

    /**
     * 根据参数位置获得参数名字
     */
    private NameProvider nameProvider;

    /**
     * 类型上下文
     */
    private TypeContext typeContext;

    /**
     * 数据源名称
     */
    private String dataSourceName;

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
     * 用于对db进行操作
     */
    protected AbstractOperator(ASTRootNode rootNode, Method method) {
        this.rootNode = rootNode;
        this.nameProvider = new NameProvider(method);
        initTypeContext(method);
        initDbAnno(method, rootNode);
        initShardBy(method, rootNode);
        rootNode.checkType(typeContext);
    }

    private void initTypeContext(Method method) {
        Type[] methodArgTypes = getMethodArgTypes(method);
        Map<String, Type> parameterTypeMap = new HashMap<String, Type>();
        for (int i = 0; i < methodArgTypes.length; i++) {
            parameterTypeMap.put(nameProvider.getParameterNameByIndex(i), methodArgTypes[i]);
        }
        typeContext = new TypeContextImpl(parameterTypeMap);
    }

    abstract Type[] getMethodArgTypes(Method method);

    /**
     * 初始化db信息
     */
    private void initDbAnno(Method method, ASTRootNode rootNode) {
        DB dbAnno = method.getDeclaringClass().getAnnotation(DB.class);
        if (dbAnno == null) {
            throw new IncorrectAnnotationException("need @DB on dao interface");
        }
        dataSourceName = dbAnno.dataSource();
        String globalTable = null;
        if (!Strings.isNullOrEmpty(dbAnno.table())) {
            globalTable = dbAnno.table();
        }
        rootNode.setGlobalTable(globalTable);
        Class<? extends TablePartition> tpc = dbAnno.tablePartition();
        if (tpc != null && !tpc.equals(IgnoreTablePartition.class)) {
            tablePartition = Reflection.instantiate(tpc);
        }
        Class<? extends DataSourceRouter> dsrc = dbAnno.dataSourceRouter();
        if (dsrc != null && !dsrc.equals(IgnoreDataSourceRouter.class)) {
            dataSourceRouter = Reflection.instantiate(dsrc);
        }

        if (tablePartition != null && globalTable == null) { // 使用了分表但没有使用全局表名则抛出异常
            throw new IncorrectDefinitionException("if @DB.tablePartition is defined, @DB.table must be defined");
        }

        if (dataSourceRouter != null && tablePartition == null) { // 使用了数据源路由但没有使用分表则抛出异常
            throw new IncorrectDefinitionException("if @DB.dataSourceRouter is defined, " +
                    "@DB.tablePartition must be defined");
        }
    }

    /**
     * 提取{@link org.jfaster.mango.annotation.ShardBy}参数
     */
    private void initShardBy(Method method, ASTRootNode rootNode) {
        Annotation[][] pass = method.getParameterAnnotations();
        int shardByNum = 0;
        for (int i = 0; i < pass.length; i++) {
            Annotation[] pas = pass[i];
            for (Annotation pa : pas) {
                if (ShardBy.class.equals(pa.annotationType())) {
                    shardParameterName = nameProvider.getParameterNameByIndex(i);
                    shardPropertyPath = ((ShardBy) pa).value();
                    shardByNum++;
                }
            }
        }
        if (tablePartition != null) {
            if (shardByNum == 1) {
                Type shardType = getTypeContext().getPropertyType(shardParameterName, shardPropertyPath);
                TypeToken typeToken = new TypeToken(shardType);
                Class<?> mappedClass = typeToken.getMappedClass();
                if (mappedClass == null || typeToken.isIterable()) {
                    throw new IncorrectParameterTypeException("the type of parameter Modified @ShardBy is error, " +
                            "type is " + shardType);
                }
                rootNode.setPartitionInfo(tablePartition, shardParameterName, shardPropertyPath);
            } else {
                throw new IncorrectDefinitionException("if @DB.tablePartition is defined, " +
                        "need one and only one @ShardBy on method's parameter but found " + shardByNum);
            }
        } else {
            if (shardByNum > 0) {
                throw new IncorrectDefinitionException("if @DB.tablePartition is not defined, " +
                        "@ShardBy can not on method's parameter but found " + shardByNum);
            }
        }
    }

    protected TypeContext getTypeContext() {
        return typeContext;
    }

    protected NameProvider getNameProvider() {
        return nameProvider;
    }

    protected RuntimeContext buildRuntimeContext(Object[] values) {
        RuntimeContext context = new RuntimeContextImpl();
        for (int i = 0; i < values.length; i++) {
            context.addParameter(nameProvider.getParameterNameByIndex(i), values[i]);
        }
        return context;
    }

    protected DataSource getDataSource(RuntimeContext context) {
        return getDataSource(getDataSourceName(context));
    }

    public DataSource getDataSource(String dataSourceName) {
        if (logger.isDebugEnabled()) {
            logger.debug("The name of Datasource is [" + dataSourceName + "]");
        }
        DataSource ds = dataSourceFactory.getDataSource(dataSourceName, rootNode.getSQLType());
        if (ds == null) {
            throw new IncorrectDefinitionException("can't find datasource for name " + dataSourceName);
        }
        return ds;
    }

    @Nullable
    public String getDataSourceName(RuntimeContext context) {
        String realDataSourceName = dataSourceRouter != null ?
                dataSourceRouter.getDataSourceName(context.getPropertyValue(shardParameterName, shardPropertyPath)) :
                dataSourceName;
        return realDataSourceName;
    }

    /**
     * 拦截器链
     */
    protected RuntimeInterceptorChain runtimeInterceptorChain;

    /**
     * 状态统计
     */
    protected StatsCounter statsCounter;

    /**
     * 数据源工厂
     */
    protected DataSourceFactory dataSourceFactory;

    /**
     * jdbc操作
     */
    protected JdbcTemplate jdbcTemplate;

    @Override
    public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void setDataSourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public void setRuntimeInterceptorChain(RuntimeInterceptorChain runtimeInterceptorChain) {
        this.runtimeInterceptorChain = runtimeInterceptorChain;
    }

    @Override
    public void setStatsCounter(StatsCounter statsCounter) {
        this.statsCounter = statsCounter;
    }

}
