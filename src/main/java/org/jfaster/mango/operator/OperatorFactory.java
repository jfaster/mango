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
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.parser.Parser;
import org.jfaster.mango.partition.IgnoreTablePartition;
import org.jfaster.mango.partition.TablePartition;
import org.jfaster.mango.util.SQLType;
import org.jfaster.mango.util.Strings;
import org.jfaster.mango.reflect.MethodDescriptor;
import org.jfaster.mango.reflect.ParameterDescriptor;
import org.jfaster.mango.reflect.Reflection;
import org.jfaster.mango.reflect.TypeWrapper;

import java.lang.reflect.Type;

/**
 * Operator工厂
 *
 * @author ash
 */
public class OperatorFactory {

    private final DataSourceFactory dataSourceFactory;
    private final CacheHandler cacheHandler;
    private final InterceptorChain queryInterceptorChain;
    private final InterceptorChain updateInterceptorChain;

    public OperatorFactory(DataSourceFactory dataSourceFactory, CacheHandler cacheHandler,
                               InterceptorChain queryInterceptorChain, InterceptorChain updateInterceptorChain) {
        this.dataSourceFactory = dataSourceFactory;
        this.cacheHandler = cacheHandler;
        this.queryInterceptorChain = queryInterceptorChain;
        this.updateInterceptorChain = updateInterceptorChain;
    }

    public Operator getOperator(MethodDescriptor md) throws Exception {
        SQL sqlAnno = md.getAnnotation(SQL.class);
        if (sqlAnno == null) {
            throw new IncorrectAnnotationException("each method expected one @SQL annotation " +
                    "but not found");
        }
        String sql = sqlAnno.value();
        if (Strings.isEmpty(sql)) {
            throw new IncorrectSqlException("sql is null or empty");
        }
        ASTRootNode rootNode = new Parser(sql.trim()).parse().init();
        SQLType sqlType = rootNode.getSQLType();

        CacheIgnored cacheIgnoredAnno = md.getAnnotation(CacheIgnored.class);
        Cache cacheAnno = md.getAnnotation(Cache.class);
        boolean useCache = cacheAnno != null && cacheIgnoredAnno == null;

        Class<?> returnType = md.getRawReturnType();
        InvocationInterceptorChain chain;
        OperatorType operatorType;
        if (sqlType == SQLType.SELECT) { // 查
            operatorType = OperatorType.QUERY;
        } else if (int.class.equals(returnType) || long.class.equals(returnType)) { // 更新
            operatorType = OperatorType.UPDATE;
        } else if (int[].class.equals(returnType)) { // 批量更新
            operatorType = OperatorType.BATCHUPDATYPE;
        } else {
            throw new IncorrectReturnTypeException("if sql don't start with select, " +
                    "update return type expected int, " +
                    "batch update return type expected int[], " +
                    "but " + md.getRawReturnType());
        }

        NameProvider nameProvider = new NameProvider(md.getParameterDescriptors());
        ParameterContext context = new ParameterContext(md.getParameterDescriptors(), nameProvider, operatorType);

        rootNode.expandParameter(context); // 扩展简化的参数节点
        rootNode.checkType(context); // 检测节点类型

        DbInfo dbInfo = getDbInfo(md, nameProvider, context);
        TableGenerator tableGenerator = new TableGenerator(dbInfo.globalTable, dbInfo.shardParameterName,
                dbInfo.shardPropertyPath, dbInfo.tablePartition);
        DataSourceGenerator dataSourceGenerator = new DataSourceGenerator(dataSourceFactory, rootNode.getSQLType(),
               dbInfo.dataSourceName, dbInfo.shardParameterName, dbInfo.shardPropertyPath, dbInfo.dataSourceRouter);

        Operator operator;
        if (useCache) {
            CacheDriverImpl driver = new CacheDriverImpl(md, rootNode, cacheHandler, context, nameProvider);
            switch (operatorType) {
                case QUERY:
                    operator = new CacheableQueryOperator(rootNode, md, driver);
                    break;
                case UPDATE:
                    operator = new CacheableUpdateOperator(rootNode, md, driver);
                    break;
                case BATCHUPDATYPE:
                    operator = new CacheableBatchUpdateOperator(rootNode, driver);
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
                case BATCHUPDATYPE:
                    operator = new BatchUpdateOperator(rootNode);
                    break;
                default:
                    throw new IllegalStateException();
            }
        }

        chain =  sqlType == SQLType.SELECT ?
                new InvocationInterceptorChain(queryInterceptorChain, context.getParameterDescriptors(), sqlType) :
                new InvocationInterceptorChain(updateInterceptorChain, context.getParameterDescriptors(), sqlType);

        operator.setTableGenerator(tableGenerator);
        operator.setDataSourceGenerator(dataSourceGenerator);
        operator.setInvocationContextFactory(new InvocationContextFactory(nameProvider));
        operator.setInvocationInterceptorChain(chain);
        return operator;
    }

    DbInfo getDbInfo(MethodDescriptor md, NameProvider nameProvider, ParameterContext context) {
        DB dbAnno = md.getAnnotation(DB.class);
        if (dbAnno == null) {
            throw new IncorrectAnnotationException("need @DB on dao interface");
        }
        String dataSourceName = dbAnno.dataSource();
        String globalTable = null;
        if (!Strings.isEmpty(dbAnno.table())) {
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

        int shardByNum = 0;
        String shardParameterName = null;
        String shardPropertyPath = null;
        for (ParameterDescriptor pd : md.getParameterDescriptors()) {
            ShardBy shardByAnno = pd.getAnnotation(ShardBy.class);
            if (shardByAnno != null) {
                shardParameterName = nameProvider.getParameterName(pd.getPosition());
                shardPropertyPath = shardByAnno.value();
                shardByNum++;
            }
        }
        if (tablePartition != null) {
            if (shardByNum == 1) {
                Type shardType = context.getPropertyType(shardParameterName, shardPropertyPath);
                TypeWrapper tw = new TypeWrapper(shardType);
                Class<?> mappedClass = tw.getMappedClass();
                if (mappedClass == null || tw.isIterable()) {
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

        return new DbInfo(shardParameterName, shardPropertyPath, globalTable,
                dataSourceName, tablePartition, dataSourceRouter);
    }

    static class DbInfo {
        String shardParameterName;
        String shardPropertyPath;
        String globalTable;
        String dataSourceName;
        TablePartition tablePartition;
        DataSourceRouter dataSourceRouter;

        DbInfo(String shardParameterName, String shardPropertyPath, String globalTable,
               String dataSourceName, TablePartition tablePartition, DataSourceRouter dataSourceRouter) {
            this.shardParameterName = shardParameterName;
            this.shardPropertyPath = shardPropertyPath;
            this.globalTable = globalTable;
            this.dataSourceName = dataSourceName;
            this.tablePartition = tablePartition;
            this.dataSourceRouter = dataSourceRouter;
        }
    }

}
