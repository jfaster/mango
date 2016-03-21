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

import org.jfaster.mango.annotation.*;
import org.jfaster.mango.datasource.DataSourceFactory;
import org.jfaster.mango.datasource.DataSourceType;
import org.jfaster.mango.exception.IncorrectDefinitionException;
import org.jfaster.mango.exception.IncorrectParameterTypeException;
import org.jfaster.mango.exception.IncorrectSqlException;
import org.jfaster.mango.invoker.GetterInvokerGroup;
import org.jfaster.mango.jdbc.JdbcOperations;
import org.jfaster.mango.operator.cache.*;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.parser.SqlParser;
import org.jfaster.mango.partition.DataSourceRouter;
import org.jfaster.mango.partition.IgnoreDataSourceRouter;
import org.jfaster.mango.partition.IgnoreTablePartition;
import org.jfaster.mango.partition.TablePartition;
import org.jfaster.mango.reflect.*;
import org.jfaster.mango.util.SQLType;
import org.jfaster.mango.util.Strings;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;

/**
 * Operator工厂
 *
 * @author ash
 */
public class OperatorFactory {

    private final DataSourceFactory dataSourceFactory;
    private final CacheHandler cacheHandler;
    private final InterceptorChain interceptorChain;
    private final JdbcOperations jdbcOperations;
    private final Config config;

    public OperatorFactory(DataSourceFactory dataSourceFactory, CacheHandler cacheHandler,
                           InterceptorChain interceptorChain, JdbcOperations jdbcOperations, Config config) {
        this.dataSourceFactory = dataSourceFactory;
        this.cacheHandler = cacheHandler;
        this.interceptorChain = interceptorChain;
        this.jdbcOperations = jdbcOperations;
        this.config = config;
    }

    public Operator getOperator(MethodDescriptor md, StatsCounter statsCounter)  {
        SQL sqlAnno = md.getAnnotation(SQL.class);
        if (sqlAnno == null) {
            throw new IllegalStateException("each method expected one @SQL annotation " +
                    "but not found");
        }
        String sql = sqlAnno.value();
        if (Strings.isEmpty(sql)) {
            throw new IncorrectSqlException("sql is null or empty");
        }
        ASTRootNode rootNode = SqlParser.parse(sql).init();
        SQLType sqlType = rootNode.getSQLType();

        List<ParameterDescriptor> pds = md.getParameterDescriptors();
        OperatorType operatorType;
        if (sqlType == SQLType.SELECT) {
            operatorType = OperatorType.QUERY;
        } else {
            operatorType = OperatorType.UPDATE;
            if (pds.size() == 1) { // 只有一个参数
                ParameterDescriptor pd = pds.get(0);
                if (pd.isIterable() && rootNode.getJDBCIterableParameters().isEmpty()) {
                    // 参数可迭代，同时sql中没有in语句
                    operatorType = OperatorType.BATCHUPDATE;
                }
            }
        }

        NameProvider nameProvider = new NameProvider(md.getParameterDescriptors());
        ParameterContext context = new ParameterContext(md.getParameterDescriptors(), nameProvider, operatorType);
        statsCounter.setOperatorType(operatorType);

        rootNode.expandParameter(context); // 扩展简化的参数节点
        rootNode.checkAndBind(context); // 绑定GetterInvoker

        TableGenerator tableGenerator = getTableGenerator(md, rootNode, nameProvider, context);
        DataSourceType dst = DataSourceType.SLAVE;
        if (sqlType.needChangeData() || md.isAnnotationPresent(UseMaster.class)) {
            dst = DataSourceType.MASTER;
        }
        DataSourceGenerator dataSourceGenerator = getDataSourceGenerator(dataSourceFactory, dst, md, nameProvider, context);

        Operator operator;
        CacheIgnored cacheIgnoredAnno = md.getAnnotation(CacheIgnored.class);
        Cache cacheAnno = md.getAnnotation(Cache.class);
        boolean useCache = cacheAnno != null && cacheIgnoredAnno == null;
        if (useCache) {
            CacheDriver driver = new CacheDriver(md, rootNode, cacheHandler, context, nameProvider, statsCounter);
            statsCounter.setCacheable(true);
            statsCounter.setUseMultipleKeys(driver.isUseMultipleKeys());
            statsCounter.setCacheNullObject(driver.isCacheNullObject());
            switch (operatorType) {
                case QUERY:
                    operator = new CacheableQueryOperator(rootNode, md, driver);
                    break;
                case UPDATE:
                    operator = new CacheableUpdateOperator(rootNode, md, driver);
                    break;
                case BATCHUPDATE:
                    operator = new CacheableBatchUpdateOperator(rootNode, md, driver);
                    break;
                default:
                    throw new IllegalStateException();
            }
        } else {
            switch (operatorType) {
                case QUERY:
                    operator = new QueryOperator(rootNode, md);
                    break;
                case UPDATE:
                    operator = new UpdateOperator(rootNode, md);
                    break;
                case BATCHUPDATE:
                    operator = new BatchUpdateOperator(rootNode, md);
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        InvocationInterceptorChain chain =
                new InvocationInterceptorChain(interceptorChain, context.getParameterDescriptors(), sqlType);
        operator.setTableGenerator(tableGenerator);
        operator.setDataSourceGenerator(dataSourceGenerator);
        operator.setInvocationContextFactory(new InvocationContextFactory(nameProvider));
        operator.setInvocationInterceptorChain(chain);
        operator.setJdbcOperations(jdbcOperations);
        operator.setStatsCounter(statsCounter);
        operator.setConfig(config);
        return operator;
    }

    TableGenerator getTableGenerator(MethodDescriptor md, ASTRootNode rootNode, NameProvider nameProvider, ParameterContext context) {
        DB dbAnno = md.getAnnotation(DB.class);
        if (dbAnno == null) {
            throw new IllegalStateException("dao interface expected one @DB " +
                    "annotation but not found");
        }
        String table = null;
        if (Strings.isNotEmpty(dbAnno.table())) {
            table = dbAnno.table();
        }
        Class<? extends TablePartition> tpc = dbAnno.tablePartition();
        TablePartition tablePartition = null;
        TypeToken<?> tablePartitionToken = null;
        if (tpc != null && !tpc.equals(IgnoreTablePartition.class)) {
            tablePartition = Reflection.instantiateClass(tpc);
            Type[] types = tpc.getGenericInterfaces();
            Type genType = types.length > 0 ? types[0] : tpc.getGenericSuperclass();
            if (genType instanceof ParameterizedType) {
                Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
                tablePartitionToken = TypeToken.of(params[0]);
            } else if (genType instanceof Class) {
                tablePartitionToken = TypeToken.of(Object.class);
            } else {
                throw new IllegalStateException();
            }
        }

        // 在@DB注解中定义了table
        boolean dbAnnoSetGlobalTable = table != null;

        // 在@DB竹节中定义了tablePartition
        boolean dbAnnoSetTablePartition = tablePartition != null;

        // 在SQL中使用了#table
        boolean sqlUseGlobalTable = !rootNode.getASTGlobalTables().isEmpty();

        if (sqlUseGlobalTable && !dbAnnoSetGlobalTable) {
            throw new IncorrectDefinitionException("if sql use global table '#table', @DB.table must be defined");
        }
        if (dbAnnoSetTablePartition && !dbAnnoSetGlobalTable) {
            throw new IncorrectDefinitionException("if @DB.tablePartition is defined, @DB.table must be defined");
        }

        int shardByNum = 0;
        String shardParameterName = null;
        String shardParameterProperty = null;
        int type = 0;
        for (ParameterDescriptor pd : md.getParameterDescriptors()) {
            ShardBy shardByAnno = pd.getAnnotation(ShardBy.class);
            TableShardBy tableShardByAnno = pd.getAnnotation(TableShardBy.class);
            if (shardByAnno != null) {
                shardParameterName = nameProvider.getParameterName(pd.getPosition());
                shardParameterProperty = shardByAnno.value();
                type = shardByAnno.type();
                shardByNum++;
            }
            if (tableShardByAnno != null) {
                shardParameterName = nameProvider.getParameterName(pd.getPosition());
                shardParameterProperty = tableShardByAnno.value();
                type = tableShardByAnno.type();
                shardByNum++;
            }
        }
        TableGenerator tableGenerator;
        if (dbAnnoSetTablePartition) {
            if (shardByNum == 1) {
                GetterInvokerGroup shardByInvokerGroup = context.getInvokerGroup(shardParameterName, shardParameterProperty);
                Type shardType = shardByInvokerGroup.getFinalType();
                TypeWrapper tw = new TypeWrapper(shardType);
                Class<?> mappedClass = tw.getMappedClass();
                if (mappedClass == null || tw.isIterable()) {
                    throw new IncorrectParameterTypeException("the type of parameter Modified @TableShardBy is error, " +
                            "type is " + shardType + ", " +
                            "please note that @ShardBy = @TableShardBy + @DataSourceShardBy");
                }
                TypeToken<?> shardToken = TypeToken.of(shardType);
                if (!tablePartitionToken.isAssignableFrom(shardToken.wrap())) {
                    throw new ClassCastException("TablePartiion[" + tpc + "]'s " +
                            "generic type[" + tablePartitionToken.getType() + "] must be assignable from " +
                            "the type of parameter Modified @TableShardBy [" + shardToken.getType() + "], " +
                            "please note that @ShardBy = @TableShardBy + @DataSourceShardBy");
                }
                tableGenerator = new PartitionalTableGenerator(table, shardParameterName, shardByInvokerGroup, tablePartition, type);
            } else {
                throw new IncorrectDefinitionException("if @DB.tablePartition is defined, " +
                        "need one and only one @TableShardBy on method's parameter but found " + shardByNum + ", " +
                        "please note that @ShardBy = @TableShardBy + @DataSourceShardBy");
            }
        } else {
            tableGenerator = new SimpleTableGenerator(table);
        }
        return tableGenerator;
    }

    DataSourceGenerator getDataSourceGenerator(DataSourceFactory dataSourceFactory, DataSourceType dataSourceType,
                                               MethodDescriptor md, NameProvider nameProvider, ParameterContext context) {
        DB dbAnno = md.getAnnotation(DB.class);
        if (dbAnno == null) {
            throw new IllegalStateException("dao interface expected one @DB " +
                    "annotation but not found");
        }
        String dataSourceName = dbAnno.dataSource();
        Class<? extends DataSourceRouter> dsrc = dbAnno.dataSourceRouter();
        DataSourceRouter dataSourceRouter = null;
        TypeToken<?> dataSourceRouterToken = null;
        if (dsrc != null && !dsrc.equals(IgnoreDataSourceRouter.class)) {
            dataSourceRouter = Reflection.instantiateClass(dsrc);
            Type[] types = dsrc.getGenericInterfaces();
            Type genType = types.length > 0 ? types[0] : dsrc.getGenericSuperclass();
            if (genType instanceof ParameterizedType) {
                Type[] params = ((ParameterizedType) genType).getActualTypeArguments();
                dataSourceRouterToken = TypeToken.of(params[0]);
            } else if (genType instanceof Class) {
                dataSourceRouterToken = TypeToken.of(Object.class);
            } else {
                throw new IllegalStateException();
            }
        }

        int shardByNum = 0;
        String shardParameterName = null;
        String shardParameterProperty = null;
        int type = 0;
        for (ParameterDescriptor pd : md.getParameterDescriptors()) {
            ShardBy shardByAnno = pd.getAnnotation(ShardBy.class);
            DataSourceShardBy tableShardByAnno = pd.getAnnotation(DataSourceShardBy.class);
            if (shardByAnno != null) {
                shardParameterName = nameProvider.getParameterName(pd.getPosition());
                shardParameterProperty = shardByAnno.value();
                type = shardByAnno.type();
                shardByNum++;
            }
            if (tableShardByAnno != null) {
                shardParameterName = nameProvider.getParameterName(pd.getPosition());
                shardParameterProperty = tableShardByAnno.value();
                type = tableShardByAnno.type();
                shardByNum++;
            }
        }
        DataSourceGenerator dataSourceGenerator;
        if (dataSourceRouter != null) {
            if (shardByNum == 1) {
                GetterInvokerGroup shardByInvokerGroup = context.getInvokerGroup(shardParameterName, shardParameterProperty);
                Type shardType = shardByInvokerGroup.getFinalType();
                TypeWrapper tw = new TypeWrapper(shardType);
                Class<?> mappedClass = tw.getMappedClass();
                if (mappedClass == null || tw.isIterable()) {
                    throw new IncorrectParameterTypeException("the type of parameter Modified @DataSourceShardBy is error, " +
                            "type is " + shardType + ", " +
                            "please note that @ShardBy = @TableShardBy + @DataSourceShardBy");
                }
                TypeToken<?> shardToken = TypeToken.of(shardType);
                if (!dataSourceRouterToken.isAssignableFrom(shardToken.wrap())) {
                    throw new ClassCastException("DataSourceRouter[" + dsrc + "]'s " +
                            "generic type[" + dataSourceRouterToken.getType() + "] must be assignable from " +
                            "the type of parameter Modified @DataSourceShardBy [" + shardToken.getType() + "], " +
                            "please note that @ShardBy = @TableShardBy + @DataSourceShardBy");
                }
                dataSourceGenerator = new RoutableDataSourceGenerator(dataSourceFactory, dataSourceType, shardParameterName, shardByInvokerGroup, dataSourceRouter, type);
            } else {
                throw new IncorrectDefinitionException("if @DB.dataSourceRouter is defined, " +
                        "need one and only one @DataSourceShardBy on method's parameter but found " + shardByNum + ", " +
                        "please note that @ShardBy = @TableShardBy + @DataSourceShardBy");
            }
        } else {
            dataSourceGenerator = new SimpleDataSourceGenerator(dataSourceFactory, dataSourceType, dataSourceName);
        }
        return dataSourceGenerator;
    }

    static class DbInfo {
        String shardParameterName;
        GetterInvokerGroup shardByInvokerGroup;
        String globalTable;
        String dataSourceName;
        TablePartition tablePartition;
        DataSourceRouter dataSourceRouter;

        DbInfo(String shardParameterName, GetterInvokerGroup shardByInvokerGroup, String globalTable,
               String dataSourceName, TablePartition tablePartition, DataSourceRouter dataSourceRouter) {
            this.shardParameterName = shardParameterName;
            this.shardByInvokerGroup = shardByInvokerGroup;
            this.globalTable = globalTable;
            this.dataSourceName = dataSourceName;
            this.tablePartition = tablePartition;
            this.dataSourceRouter = dataSourceRouter;
        }
    }

}
