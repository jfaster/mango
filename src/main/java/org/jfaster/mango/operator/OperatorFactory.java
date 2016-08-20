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
import org.jfaster.mango.sharding.*;
import org.jfaster.mango.reflect.*;
import org.jfaster.mango.util.SQLType;
import org.jfaster.mango.util.Strings;

import javax.annotation.Nullable;
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

        TableShardingStrategy strategy = getTableShardingStrategy(md);
        TypeToken<?> strategyToken = null;
        if (strategy != null) {
            strategyToken = TypeToken.of(strategy.getClass()).resolveFatherClass(TableShardingStrategy.class);
        }

        // 是否配置使用全局表
        boolean isUseGlobalTable = table != null;

        // 是否配置使用表切分
        boolean isUseTableShardingStrategy = strategy != null;

        // 是否在SQL中使用#table全局表
        boolean isSqlUseGlobalTable = !rootNode.getASTGlobalTables().isEmpty();

        if (isSqlUseGlobalTable && !isUseGlobalTable) {
            throw new IncorrectDefinitionException("if sql use global table '#table'," +
                    " @DB.table must be defined");
        }
        if (isUseTableShardingStrategy && !isUseGlobalTable) {
            throw new IncorrectDefinitionException("if @Sharding.tableShardingStrategy is defined, " +
                    "@DB.table must be defined");
        }

        int shardingParameterNum = 0;
        String shardingParameterName = null;
        String shardingParameterProperty = null;
        for (ParameterDescriptor pd : md.getParameterDescriptors()) {
            TableShardingBy tableShardingByAnno = pd.getAnnotation(TableShardingBy.class);
            if (tableShardingByAnno != null) {
                shardingParameterName = nameProvider.getParameterName(pd.getPosition());
                shardingParameterProperty = tableShardingByAnno.value();
                shardingParameterNum++;
                continue; // 有了@TableShardingBy，则忽略@ShardingBy
            }
            ShardingBy shardingByAnno = pd.getAnnotation(ShardingBy.class);
            if (shardingByAnno != null) {
                shardingParameterName = nameProvider.getParameterName(pd.getPosition());
                shardingParameterProperty = shardingByAnno.value();
                shardingParameterNum++;
            }
        }
        TableGenerator tableGenerator;
        if (isUseTableShardingStrategy) {
            if (shardingParameterNum == 1) {
                GetterInvokerGroup shardingParameterInvoker
                        = context.getInvokerGroup(shardingParameterName, shardingParameterProperty);
                Type shardingParameterType = shardingParameterInvoker.getTargetType();
                TypeWrapper tw = new TypeWrapper(shardingParameterType);
                Class<?> mappedClass = tw.getMappedClass();
                if (mappedClass == null || tw.isIterable()) {
                    throw new IncorrectParameterTypeException("the type of parameter Modified @TableShardingBy is error, " +
                            "type is " + shardingParameterType + ", " +
                            "please note that @ShardingBy = @TableShardingBy + @DatabaseShardingBy");
                }
                TypeToken<?> shardToken = TypeToken.of(shardingParameterType);
                if (!strategyToken.isAssignableFrom(shardToken.wrap())) {
                    throw new ClassCastException("TableShardingStrategy[" + strategy.getClass() + "]'s " +
                            "generic type[" + strategyToken.getType() + "] must be assignable from " +
                            "the type of parameter Modified @TableShardingBy [" + shardToken.getType() + "], " +
                            "please note that @ShardingBy = @TableShardingBy + @DatabaseShardingBy");
                }
                tableGenerator = new PartitionalTableGenerator(table, shardingParameterName, shardingParameterInvoker, strategy);
            } else {
                throw new IncorrectDefinitionException("if @Sharding.tableShardingStrategy is defined, " +
                        "need one and only one @TableShardingBy on method's parameter but found " + shardingParameterNum + ", " +
                        "please note that @ShardingBy = @TableShardingBy + @DatabaseShardingBy");
            }
        } else {
            tableGenerator = new SimpleTableGenerator(table);
        }
        return tableGenerator;
    }

    @Nullable
    private TableShardingStrategy getTableShardingStrategy(MethodDescriptor md) {
        Sharding shardingAnno = md.getAnnotation(Sharding.class);
        if (shardingAnno == null) {
            return null;
        }
        Class<? extends TableShardingStrategy> strategyClass = shardingAnno.tableShardingStrategy();
        if (!strategyClass.equals(NotUseTableShardingStrategy.class)) {
            TableShardingStrategy strategy = Reflection.instantiateClass(strategyClass);
            return strategy;
        }
        strategyClass = shardingAnno.shardingStrategy();
        if (!strategyClass.equals(NotUseShardingStrategy.class)) {
            TableShardingStrategy strategy = Reflection.instantiateClass(strategyClass);
            return strategy;
        }
        return null;
    }

    DataSourceGenerator getDataSourceGenerator(DataSourceFactory dataSourceFactory, DataSourceType dataSourceType,
                                               MethodDescriptor md, NameProvider nameProvider, ParameterContext context) {
        DB dbAnno = md.getAnnotation(DB.class);
        if (dbAnno == null) {
            throw new IllegalStateException("dao interface expected one @DB " +
                    "annotation but not found");
        }
        String database = dbAnno.database();


        DatabaseShardingStrategy strategy = getDatabaseShardingStrategy(md);
        TypeToken<?> strategyToken = null;
        if (strategy != null) {
            strategyToken = TypeToken.of(strategy.getClass()).resolveFatherClass(DatabaseShardingStrategy.class);
        }

        int shardingParameterNum = 0;
        String shardingParameterName = null;
        String shardingParameterProperty = null;
        for (ParameterDescriptor pd : md.getParameterDescriptors()) {
            DatabaseShardingBy databaseShardingByAnno = pd.getAnnotation(DatabaseShardingBy.class);
            if (databaseShardingByAnno != null) {
                shardingParameterName = nameProvider.getParameterName(pd.getPosition());
                shardingParameterProperty = databaseShardingByAnno.value();
                shardingParameterNum++;
                continue; // 有了@DatabaseShardingBy，则忽略@ShardingBy
            }
            ShardingBy shardingByAnno = pd.getAnnotation(ShardingBy.class);
            if (shardingByAnno != null) {
                shardingParameterName = nameProvider.getParameterName(pd.getPosition());
                shardingParameterProperty = shardingByAnno.value();
                shardingParameterNum++;
            }
        }
        DataSourceGenerator dataSourceGenerator;
        if (strategy != null) {
            if (shardingParameterNum == 1) {
                GetterInvokerGroup shardingParameterInvoker
                        = context.getInvokerGroup(shardingParameterName, shardingParameterProperty);
                Type shardingParameterType = shardingParameterInvoker.getTargetType();
                TypeWrapper tw = new TypeWrapper(shardingParameterType);
                Class<?> mappedClass = tw.getMappedClass();
                if (mappedClass == null || tw.isIterable()) {
                    throw new IncorrectParameterTypeException("the type of parameter Modified @DatabaseShardingBy is error, " +
                            "type is " + shardingParameterType + ", " +
                            "please note that @ShardingBy = @TableShardingBy + @DatabaseShardingBy");
                }
                TypeToken<?> shardToken = TypeToken.of(shardingParameterType);
                if (!strategyToken.isAssignableFrom(shardToken.wrap())) {
                    throw new ClassCastException("DatabaseShardingStrategy[" + strategy.getClass() + "]'s " +
                            "generic type[" + strategyToken.getType() + "] must be assignable from " +
                            "the type of parameter Modified @DatabaseShardingBy [" + shardToken.getType() + "], " +
                            "please note that @ShardingBy = @TableShardingBy + @DatabaseShardingBy");
                }
                dataSourceGenerator = new RoutableDataSourceGenerator(dataSourceFactory, dataSourceType, shardingParameterName, shardingParameterInvoker, strategy);
            } else {
                throw new IncorrectDefinitionException("if @Sharding.databaseShardingStrategy is defined, " +
                        "need one and only one @DatabaseShardingBy on method's parameter but found " + shardingParameterNum + ", " +
                        "please note that @ShardingBy = @TableShardingBy + @DatabaseShardingBy");
            }
        } else {
            dataSourceGenerator = new SimpleDataSourceGenerator(dataSourceFactory, dataSourceType, database);
        }
        return dataSourceGenerator;
    }

    @Nullable
    private DatabaseShardingStrategy getDatabaseShardingStrategy(MethodDescriptor md) {
        Sharding shardingAnno = md.getAnnotation(Sharding.class);
        if (shardingAnno == null) {
            return null;
        }
        Class<? extends DatabaseShardingStrategy> strategyClass = shardingAnno.databaseShardingStrategy();
        if (!strategyClass.equals(NotUseDatabaseShardingStrategy.class)) {
            DatabaseShardingStrategy strategy = Reflection.instantiateClass(strategyClass);
            return strategy;
        }
        strategyClass = shardingAnno.shardingStrategy();
        if (!strategyClass.equals(NotUseShardingStrategy.class)) {
            DatabaseShardingStrategy strategy = Reflection.instantiateClass(strategyClass);
            return strategy;
        }
        return null;
    }

}
