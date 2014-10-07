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
import org.jfaster.mango.cache.CacheHandler;
import org.jfaster.mango.datasource.factory.DataSourceFactory;
import org.jfaster.mango.datasource.router.DataSourceRouter;
import org.jfaster.mango.datasource.router.IgnoreDataSourceRouter;
import org.jfaster.mango.exception.*;
import org.jfaster.mango.jdbc.JdbcTemplate;
import org.jfaster.mango.operator.cache.CacheDriverImpl;
import org.jfaster.mango.operator.cache.CacheableBatchUpdateOperator;
import org.jfaster.mango.operator.cache.CacheableQueryOperator;
import org.jfaster.mango.operator.cache.CacheableUpdateOperator;
import org.jfaster.mango.operator.interceptor.InterceptorChain;
import org.jfaster.mango.operator.interceptor.RuntimeInterceptorChain;
import org.jfaster.mango.operator.stats.StatsCounter;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.parser.Parser;
import org.jfaster.mango.partition.IgnoreTablePartition;
import org.jfaster.mango.partition.TablePartition;
import org.jfaster.mango.util.SQLType;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.util.reflect.Reflection;
import org.jfaster.mango.util.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

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

        Class<?> returnType = method.getReturnType();
        RuntimeInterceptorChain chain;
        OperatorType operatorType;
        if (rootNode.getSQLType() == SQLType.SELECT) { // 查
            operatorType = OperatorType.QUERY;
            chain = new RuntimeInterceptorChain(queryInterceptorChain, method);
        } else if (int.class.equals(returnType) || long.class.equals(returnType)) { // 更新
            operatorType = OperatorType.UPDATE;
            chain = new RuntimeInterceptorChain(updateInterceptorChain, method);
        } else if (int[].class.equals(returnType)) { // 批量更新
            operatorType = OperatorType.BATCHUPDATYPE;
            chain = new RuntimeInterceptorChain(updateInterceptorChain, method);
        } else {
            throw new IncorrectReturnTypeException("if sql don't start with select, " +
                    "update return type expected int, " +
                    "batch update return type expected int[], " +
                    "but " + method.getReturnType());
        }

        NameProvider nameProvider = buildNameProvider(method);
        TypeContext typeContext = buildTypeContext(method, nameProvider, operatorType);
        rootNode.checkType(typeContext);
        TableGenerator tableGenerator = new TableGenerator();
        DataSourceGenerator dataSourceGenerator = new DataSourceGenerator(dataSourceFactory, rootNode.getSQLType());
        fill(method, tableGenerator, dataSourceGenerator, nameProvider, typeContext);

        Operator operator;
        if (useCache) {
            CacheDriverImpl driver = new CacheDriverImpl(method, rootNode, cacheHandler, typeContext, nameProvider);
            switch (operatorType) {
                case QUERY:
                    operator = new CacheableQueryOperator(rootNode, method, driver);
                    break;
                case UPDATE:
                    operator = new CacheableUpdateOperator(rootNode, method, driver);
                    break;
                case BATCHUPDATYPE:
                    operator = new CacheableBatchUpdateOperator(rootNode, driver);
                    break;
                default:
                    throw new IllegalStateException(); // TODO
            }
        } else {
            switch (operatorType) {
                case QUERY:
                    operator = new QueryOperator(rootNode, method);
                    break;
                case UPDATE:
                    operator = new UpdateOperator(rootNode, method);
                    break;
                case BATCHUPDATYPE:
                    operator = new BatchUpdateOperator(rootNode);
                    break;
                default:
                    throw new IllegalStateException(); // TODO
            }
        }

        operator.setJdbcTemplate(new JdbcTemplate());
        operator.setDataSourceGenerator(dataSourceGenerator);
        operator.setRuntimeContextFactory(new RuntimeContextFactoryImpl(nameProvider, tableGenerator));
        operator.setRuntimeInterceptorChain(chain);
        operator.setStatsCounter(statsCounter);
        return operator;
    }

    NameProvider buildNameProvider(Method method) {
        Annotation[][] pass = method.getParameterAnnotations();
        NameProvider np = new NameProvider();
        for (int i = 0; i < pass.length; i++) {
            Annotation[] pas = pass[i];
            for (Annotation pa : pas) {
                if (Rename.class.equals(pa.annotationType())) {
                    np.setParameterName(i, ((Rename) pa).value());
                }
            }
        }
        return np;
    }

    TypeContext buildTypeContext(Method method, NameProvider nameProvider, OperatorType operatorType) {
        Type[] types = method.getGenericParameterTypes();
        if (operatorType == OperatorType.BATCHUPDATYPE) {
            if (types.length != 1) {
                throw new IncorrectParameterCountException("batch update expected one and only one parameter but " +
                        types.length); // 批量更新只能有一个参数
            }
            Type type = types[0];
            TypeToken typeToken = new TypeToken(type);
            Class<?> mappedClass = typeToken.getMappedClass();
            if (mappedClass == null || !typeToken.isIterable()) {
                throw new IncorrectParameterTypeException("parameter of batch update " +
                        "expected array or implementations of java.util.List or implementations of java.util.Set " +
                        "but " + type); // 批量更新的参数必须可迭代
            }
            types = new Type[]{mappedClass};
        }
        TypeContextImpl typeContext = new TypeContextImpl();
        for (int i = 0; i < types.length; i++) {
            typeContext.addParameter(nameProvider.getParameterName(i), types[i]);
        }
        return typeContext;
    }

    void fill(Method method, TableGenerator tableGenerator, DataSourceGenerator dataSourceGenerator,
              NameProvider nameProvider, TypeContext typeContext) {
        DB dbAnno = method.getDeclaringClass().getAnnotation(DB.class);
        if (dbAnno == null) {
            throw new IncorrectAnnotationException("need @DB on dao interface");
        }
        String dataSourceName = dbAnno.dataSource();
        String globalTable = null;
        if (!Strings.isNullOrEmpty(dbAnno.table())) {
            globalTable = dbAnno.table();
        }
        Class<? extends TablePartition> tpc = dbAnno.tablePartition();
        TablePartition tablePartition = null;
        if (tpc != null && !tpc.equals(IgnoreTablePartition.class)) {
            tablePartition = Reflection.instantiate(tpc);
        }
        Class<? extends DataSourceRouter> dsrc = dbAnno.dataSourceRouter();
        DataSourceRouter dataSourceRouter = null;
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

        Annotation[][] pass = method.getParameterAnnotations();
        int shardByNum = 0;
        String shardParameterName = null;
        String shardPropertyPath = null;
        for (int i = 0; i < pass.length; i++) {
            Annotation[] pas = pass[i];
            for (Annotation pa : pas) {
                if (ShardBy.class.equals(pa.annotationType())) {
                    shardParameterName = nameProvider.getParameterName(i);
                    shardPropertyPath = ((ShardBy) pa).value();
                    shardByNum++;
                }
            }
        }
        if (tablePartition != null) {
            if (shardByNum == 1) {
                Type shardType = typeContext.getPropertyType(shardParameterName, shardPropertyPath);
                TypeToken typeToken = new TypeToken(shardType);
                Class<?> mappedClass = typeToken.getMappedClass();
                if (mappedClass == null || typeToken.isIterable()) {
                    throw new IncorrectParameterTypeException("the type of parameter Modified @ShardBy is error, " +
                            "type is " + shardType);
                }
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

        tableGenerator.setTable(globalTable);
        tableGenerator.setShardParameterName(shardParameterName);
        tableGenerator.setShardPropertyPath(shardPropertyPath);
        tableGenerator.setTablePartition(tablePartition);

        dataSourceGenerator.setDataSourceName(dataSourceName);
        dataSourceGenerator.setShardParameterName(shardParameterName);
        dataSourceGenerator.setShardPropertyPath(shardPropertyPath);
        dataSourceGenerator.setDataSourceRouter(dataSourceRouter);
    }

    enum OperatorType {

        QUERY,

        UPDATE,

        BATCHUPDATYPE,

    }

}
