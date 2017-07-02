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

import org.jfaster.mango.annotation.Column;
import org.jfaster.mango.annotation.Mapper;
import org.jfaster.mango.annotation.Result;
import org.jfaster.mango.annotation.Results;
import org.jfaster.mango.binding.BoundSql;
import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.descriptor.MethodDescriptor;
import org.jfaster.mango.descriptor.ReturnDescriptor;
import org.jfaster.mango.jdbc.*;
import org.jfaster.mango.mapper.BeanPropertyRowMapper;
import org.jfaster.mango.mapper.RowMapper;
import org.jfaster.mango.mapper.SingleColumnRowMapper;
import org.jfaster.mango.parser.ASTRootNode;
import org.jfaster.mango.parser.EmptyObjectException;
import org.jfaster.mango.stat.InvocationStat;
import org.jfaster.mango.type.TypeHandlerRegistry;
import org.jfaster.mango.util.bean.BeanUtil;
import org.jfaster.mango.util.bean.PropertyMeta;
import org.jfaster.mango.util.reflect.Reflection;

import javax.sql.DataSource;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.Map;

/**
 * @author ash
 */
public class QueryOperator extends AbstractOperator {

  protected RowMapper<?> rowMapper;
  protected ReturnDescriptor returnDescriptor;
  protected ListSupplier listSupplier;
  protected SetSupplier setSupplier;

  public QueryOperator(ASTRootNode rootNode, MethodDescriptor md, Config config) {
    super(rootNode, md, config);
    init(md);
  }

  private void init(MethodDescriptor md) {
    returnDescriptor = md.getReturnDescriptor();
    rowMapper = getRowMapper(returnDescriptor.getMappedClass(), returnDescriptor);
    if (returnDescriptor.isCollection()
        || returnDescriptor.isList()
        || returnDescriptor.isLinkedList()) {
      listSupplier = new LinkedListSuppliter();
    } else if (returnDescriptor.isArrayList()) {
      listSupplier = new ArrayListSuppliter();
    } else if (returnDescriptor.isSetAssignable()) {
      setSupplier = new HashSetSupplier();
    }
  }

  @Override
  public Object execute(Object[] values, InvocationStat stat) {
    InvocationContext context = invocationContextFactory.newInvocationContext(values);
    return execute(context, stat);
  }

  protected Object execute(InvocationContext context, InvocationStat stat) {
    context.setGlobalTable(tableGenerator.getTable(context));

    try {
      rootNode.render(context);
    } catch (EmptyObjectException e) {
      if (config.isCompatibleWithEmptyList()) {
        return EmptyObject();
      } else {
        throw e;
      }
    }

    BoundSql boundSql = context.getBoundSql();
    DataSource ds = dataSourceGenerator.getDataSource(context, daoClass);
    invocationInterceptorChain.intercept(boundSql, context, ds); // 拦截器
    return executeFromDb(ds, boundSql, stat);
  }

  private Object executeFromDb(final DataSource ds, final BoundSql boundSql, InvocationStat stat) {
    Object r;
    boolean success = false;
    long now = System.nanoTime();
    try {

      r = new QueryVisitor() {

        @Override
        Object visitForList() {
          return jdbcOperations.queryForList(ds, boundSql, listSupplier, rowMapper);
        }

        @Override
        Object visitForSet() {
          return jdbcOperations.queryForSet(ds, boundSql, setSupplier, rowMapper);
        }

        @Override
        Object visitForArray() {
          return jdbcOperations.queryForArray(ds, boundSql, rowMapper);

        }

        @Override
        Object visitForObject() {
          return jdbcOperations.queryForObject(ds, boundSql, rowMapper);
        }
      }.visit();

      success = true;
    } finally {
      long cost = System.nanoTime() - now;
      if (success) {
        stat.recordDatabaseExecuteSuccess(cost);
      } else {
        stat.recordDatabaseExecuteException(cost);
      }
    }
    return r;
  }

  private <T> RowMapper<?> getRowMapper(Class<T> clazz, ReturnDescriptor rd) {
    Mapper mapperAnno = rd.getAnnotation(Mapper.class);
    if (mapperAnno != null) { // 自定义mapper
      return Reflection.instantiateClass(mapperAnno.value());
    }

    if (TypeHandlerRegistry.hasTypeHandler(clazz)) { // 单列mapper
      return new SingleColumnRowMapper<T>(clazz);
    }

    // 类属性mapper
    Results resultsAnoo = rd.getAnnotation(Results.class);
    Map<String, String> ptc = getPropToColMap(clazz);
    if (resultsAnoo != null) {
      Result[] resultAnnos = resultsAnoo.value();
      if (resultAnnos != null) {
        for (Result resultAnno : resultAnnos) {
          ptc.put(resultAnno.property().trim(),
              resultAnno.column().trim());
        }
      }
    }
    return new BeanPropertyRowMapper<T>(clazz, ptc, config.isCheckColumn());
  }

  private Map<String, String> getPropToColMap(Class<?> clazz) {
    Map<String, String> propToColMap = new HashMap<String, String>();
    for (PropertyMeta propertyMeta : BeanUtil.fetchPropertyMetas(clazz)) {
      Column colAnno = propertyMeta.getPropertyAnno(Column.class);
      if (colAnno != null) {
        String prop = propertyMeta.getName();
        String col = colAnno.value();
        propToColMap.put(prop, col);
      }
    }
    return propToColMap;
  }

  protected Object EmptyObject() {
    return new QueryVisitor() {
      @Override
      Object visitForList() {
        return listSupplier.get(rowMapper.getMappedClass());
      }

      @Override
      Object visitForSet() {
        return setSupplier.get(rowMapper.getMappedClass());
      }

      @Override
      Object visitForArray() {
        return Array.newInstance(rowMapper.getMappedClass(), 0);
      }

      @Override
      Object visitForObject() {
        return null;
      }
    }.visit();
  }

  abstract class QueryVisitor {

    public Object visit() {
      Object r;
      if (returnDescriptor.isCollection()
          || returnDescriptor.isListAssignable()) {
        r = visitForList();
      } else if (returnDescriptor.isSetAssignable()) {
        r = visitForSet();
      } else if (returnDescriptor.isArray()) {
        r = visitForArray();
      } else {
        r = visitForObject();
      }
      return r;
    }

    abstract Object visitForList();

    abstract Object visitForSet();

    abstract Object visitForArray();

    abstract Object visitForObject();

  }

}
