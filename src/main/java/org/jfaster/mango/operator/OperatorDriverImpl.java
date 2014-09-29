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
import org.jfaster.mango.annotation.Rename;
import org.jfaster.mango.annotation.ShardBy;
import org.jfaster.mango.datasource.DataSourceFactoryHolder;
import org.jfaster.mango.datasource.factory.DataSourceFactory;
import org.jfaster.mango.datasource.router.DataSourceRouter;
import org.jfaster.mango.datasource.router.IgnoreDataSourceRouter;
import org.jfaster.mango.exception.IncorrectAnnotationException;
import org.jfaster.mango.exception.IncorrectDefinitionException;
import org.jfaster.mango.exception.IncorrectParameterCountException;
import org.jfaster.mango.exception.IncorrectParameterTypeException;
import org.jfaster.mango.parser.node.ASTRootNode;
import org.jfaster.mango.partition.IgnoreTablePartition;
import org.jfaster.mango.partition.TablePartition;
import org.jfaster.mango.support.*;
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
public class OperatorDriverImpl implements OperatorDriver {

    private final static InternalLogger logger = InternalLoggerFactory.getInstance(OperatorDriverImpl.class);

    /**
     * 数据源工厂
     */
    private DataSourceFactoryHolder dataSourceFactoryHolder;

    /**
     * sql类型，对应着增删改查
     */
    protected SQLType sqlType;

    /**
     * 变量别名
     */
    private String[] aliases;

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


    public OperatorDriverImpl(DataSourceFactoryHolder dataSourceFactoryHolder, SQLType sqlType,
                              OperatorType operatorType, Method method, ASTRootNode rootNode) {
        this.dataSourceFactoryHolder = dataSourceFactoryHolder;
        this.sqlType = sqlType;
        initAlias(method);
        initTypeContext(operatorType, method);
        initDbAnno(method, rootNode);
        initShardBy(method, rootNode);
        rootNode.checkType(typeContext);
    }

    @Override
    public TypeContext getTypeContext() {
        return typeContext;
    }

    @Override
    public RuntimeContext buildRuntimeContext(Object[] values) {
        RuntimeContext context = new RuntimeContextImpl();
        for (int i = 0; i < values.length; i++) {
            context.addParameter(getParameterNameByIndex(i), values[i]);
        }
        return context;
    }

    @Override
    public DataSource getDataSource(RuntimeContext context) {
        return getDataSource(getDataSourceName(context));
    }

    @Override
    public DataSource getDataSource(String dataSourceName) {
        if (logger.isDebugEnabled()) {
            logger.debug("The name of Datasource is [" + dataSourceName + "]");
        }
        final DataSourceFactory dataSourceFactory = dataSourceFactoryHolder.get();
        DataSource ds = dataSourceFactory.getDataSource(dataSourceName, sqlType);
        if (ds == null) {
            throw new IncorrectDefinitionException("can't find datasource for name " + dataSourceName);
        }
        return ds;
    }

    @Nullable
    @Override
    public String getDataSourceName(RuntimeContext context) {
        String realDataSourceName = dataSourceRouter != null ?
                dataSourceRouter.getDataSourceName(context.getPropertyValue(shardParameterName, shardPropertyPath)) :
                dataSourceName;
        return realDataSourceName;
    }

    protected String getParameterNameByIndex(int index) {
        String alias = aliases[index];
        return alias != null ? alias : String.valueOf(index + 1);
    }

    private void initTypeContext(OperatorType operatorType, Method method) {
        Type[] methodArgTypes;
        if (operatorType != OperatorType.BATCHUPDATE) {
            methodArgTypes = method.getGenericParameterTypes();
        } else {
            if (method.getGenericParameterTypes().length != 1) {
                throw new IncorrectParameterCountException("batch update expected one and only one parameter but " +
                        method.getGenericParameterTypes().length); // 批量更新只能有一个参数
            }
            Type type = method.getGenericParameterTypes()[0];
            TypeToken typeToken = new TypeToken(type);
            Class<?> mappedClass = typeToken.getMappedClass();
            if (mappedClass == null || !typeToken.isIterable()) {
                throw new IncorrectParameterTypeException("parameter of batch update " +
                        "expected array or implementations of java.util.List or implementations of java.util.Set " +
                        "but " + type); // 批量更新的参数必须可迭代
            }
            methodArgTypes = new Type[]{mappedClass};
        }

        Map<String, Type> parameterTypeMap = new HashMap<String, Type>();
        for (int i = 0; i < methodArgTypes.length; i++) {
            parameterTypeMap.put(getParameterNameByIndex(i), methodArgTypes[i]);
        }
        typeContext = new TypeContextImpl(parameterTypeMap);
    }

    /**
     * 初始化别名
     */
    private void initAlias(Method method) {
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
                    shardParameterName = getParameterNameByIndex(i);
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

}
