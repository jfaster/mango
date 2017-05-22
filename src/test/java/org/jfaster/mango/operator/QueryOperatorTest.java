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

import org.jfaster.mango.binding.BoundSql;
import org.jfaster.mango.datasource.DataSourceFactoryGroup;
import org.jfaster.mango.datasource.SimpleDataSourceFactory;
import org.jfaster.mango.descriptor.MethodDescriptor;
import org.jfaster.mango.descriptor.ParameterDescriptor;
import org.jfaster.mango.descriptor.ReturnDescriptor;
import org.jfaster.mango.interceptor.InterceptorChain;
import org.jfaster.mango.jdbc.ListSupplier;
import org.jfaster.mango.jdbc.SetSupplier;
import org.jfaster.mango.mapper.RowMapper;
import org.jfaster.mango.stat.MetaStat;
import org.jfaster.mango.stat.InvocationStat;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.JdbcOperationsAdapter;
import org.jfaster.mango.support.MockDB;
import org.jfaster.mango.support.MockSQL;
import org.jfaster.mango.support.model4table.User;
import org.jfaster.mango.util.reflect.TypeToken;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

/**
 * @author ash
 */
public class QueryOperatorTest {

  @Test
  public void testQueryObject() throws Exception {
    TypeToken<User> t = TypeToken.of(User.class);
    String srcSql = "select * from user where id=:1.id and name=:1.name";
    AbstractOperator operator = getOperator(t, t, srcSql, new ArrayList<Annotation>());

    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @Override
      public <T> T queryForObject(DataSource ds, BoundSql boundSql, RowMapper<T> rowMapper) {
        String sql = boundSql.getSql();
        Object[] args = boundSql.getArgs().toArray();
        String descSql = "select * from user where id=? and name=?";
        assertThat(sql, equalTo(descSql));
        assertThat(args.length, equalTo(2));
        assertThat(args[0], equalTo((Object) 100));
        assertThat(args[1], equalTo((Object) "ash"));
        assertThat(rowMapper.getMappedClass().equals(User.class), is(true));
        return null;
      }
    });

    User user = new User();
    user.setId(100);
    user.setName("ash");
    operator.execute(new Object[]{user}, InvocationStat.create());
  }

  @Test
  public void testQueryList() throws Exception {
    TypeToken<User> pt = TypeToken.of(User.class);
    TypeToken<List<User>> rt = new TypeToken<List<User>>() {
    };
    String srcSql = "select * from user where id=:1.id and name=:1.name";
    AbstractOperator operator = getOperator(pt, rt, srcSql, new ArrayList<Annotation>());

    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @Override
      public <T> List<T> queryForList(DataSource ds, BoundSql boundSql,
                                      ListSupplier listSupplier, RowMapper<T> rowMapper) {
        String sql = boundSql.getSql();
        Object[] args = boundSql.getArgs().toArray();
        String descSql = "select * from user where id=? and name=?";
        assertThat(sql, equalTo(descSql));
        assertThat(args.length, equalTo(2));
        assertThat(args[0], equalTo((Object) 100));
        assertThat(args[1], equalTo((Object) "ash"));
        assertThat(rowMapper.getMappedClass().equals(User.class), is(true));
        return null;
      }
    });

    User user = new User();
    user.setId(100);
    user.setName("ash");
    operator.execute(new Object[]{user}, InvocationStat.create());
  }

  @Test
  public void testQuerySet() throws Exception {
    TypeToken<User> pt = TypeToken.of(User.class);
    TypeToken<Set<User>> rt = new TypeToken<Set<User>>() {
    };
    String srcSql = "select * from user where id=:1.id and name=:1.name";
    AbstractOperator operator = getOperator(pt, rt, srcSql, new ArrayList<Annotation>());

    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @Override
      public <T> Set<T> queryForSet(DataSource ds, BoundSql boundSql,
                                    SetSupplier setSupplier, RowMapper<T> rowMapper) {
        String sql = boundSql.getSql();
        Object[] args = boundSql.getArgs().toArray();
        String descSql = "select * from user where id=? and name=?";
        assertThat(sql, equalTo(descSql));
        assertThat(args.length, equalTo(2));
        assertThat(args[0], equalTo((Object) 100));
        assertThat(args[1], equalTo((Object) "ash"));
        assertThat(rowMapper.getMappedClass().equals(User.class), is(true));
        return null;
      }
    });

    User user = new User();
    user.setId(100);
    user.setName("ash");
    operator.execute(new Object[]{user}, InvocationStat.create());
  }

  @Test
  public void testQueryArray() throws Exception {
    TypeToken<User> pt = TypeToken.of(User.class);
    TypeToken<User[]> rt = TypeToken.of(User[].class);
    String srcSql = "select * from user where id=:1.id and name=:1.name";
    AbstractOperator operator = getOperator(pt, rt, srcSql, new ArrayList<Annotation>());

    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @Override
      public <T> Object queryForArray(DataSource ds, BoundSql boundSql, RowMapper<T> rowMapper) {
        String sql = boundSql.getSql();
        Object[] args = boundSql.getArgs().toArray();
        String descSql = "select * from user where id=? and name=?";
        assertThat(sql, equalTo(descSql));
        assertThat(args.length, equalTo(2));
        assertThat(args[0], equalTo((Object) 100));
        assertThat(args[1], equalTo((Object) "ash"));
        assertThat(rowMapper.getMappedClass().equals(User.class), is(true));
        return null;
      }
    });

    User user = new User();
    user.setId(100);
    user.setName("ash");
    operator.execute(new Object[]{user}, InvocationStat.create());
  }

  @Test
  public void testQueryIn() throws Exception {
    TypeToken<List<Integer>> pt = new TypeToken<List<Integer>>() {
    };
    TypeToken<List<User>> rt = new TypeToken<List<User>>() {
    };
    String srcSql = "select * from user where id in (:1)";
    AbstractOperator operator = getOperator(pt, rt, srcSql, new ArrayList<Annotation>());

    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @Override
      public <T> List<T> queryForList(DataSource ds, BoundSql boundSql,
                                      ListSupplier listSupplier, RowMapper<T> rowMapper) {
        String sql = boundSql.getSql();
        Object[] args = boundSql.getArgs().toArray();
        String descSql = "select * from user where id in (?,?,?)";
        assertThat(sql, equalTo(descSql));
        assertThat(args.length, equalTo(3));
        assertThat(args[0], equalTo((Object) 100));
        assertThat(args[1], equalTo((Object) 200));
        assertThat(args[2], equalTo((Object) 300));
        assertThat(rowMapper.getMappedClass().equals(User.class), is(true));
        return null;
      }
    });

    List<Integer> ids = Arrays.asList(100, 200, 300);
    operator.execute(new Object[]{ids}, InvocationStat.create());
  }

  @Test
  public void testQueryInCount() throws Exception {
    TypeToken<List<Integer>> pt = new TypeToken<List<Integer>>() {
    };
    TypeToken<Integer> rt = new TypeToken<Integer>() {
    };
    String srcSql = "select count(1) from user where id in (:1)";
    AbstractOperator operator = getOperator(pt, rt, srcSql, new ArrayList<Annotation>());

    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @SuppressWarnings("unchecked")
      @Override
      public <T> T queryForObject(DataSource ds, BoundSql boundSql, RowMapper<T> rowMapper) {
        String sql = boundSql.getSql();
        Object[] args = boundSql.getArgs().toArray();
        String descSql = "select count(1) from user where id in (?,?,?)";
        assertThat(sql, equalTo(descSql));
        assertThat(args.length, equalTo(3));
        assertThat(args[0], equalTo((Object) 100));
        assertThat(args[1], equalTo((Object) 200));
        assertThat(args[2], equalTo((Object) 300));
        assertThat(rowMapper.getMappedClass().equals(Integer.class), is(true));
        return (T) Integer.valueOf(3);
      }
    });

    List<Integer> ids = Arrays.asList(100, 200, 300);
    Integer r = (Integer) operator.execute(new Object[]{ids}, InvocationStat.create());
    assertThat(r, is(3));
  }

  @Test
  public void testStatsCounter() throws Exception {
    TypeToken<User> t = TypeToken.of(User.class);
    String srcSql = "select * from user where id=:1.id and name=:1.name";
    AbstractOperator operator = getOperator(t, t, srcSql, new ArrayList<Annotation>());

    User user = new User();
    user.setId(100);
    user.setName("ash");

    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @Override
      public <T> T queryForObject(DataSource ds, BoundSql boundSql, RowMapper<T> rowMapper) {
        return null;
      }
    });
    InvocationStat stat = InvocationStat.create();
    operator.execute(new Object[]{user}, stat);
    assertThat(stat.getDatabaseExecuteSuccessCount(), equalTo(1L));
    operator.execute(new Object[]{user}, stat);
    assertThat(stat.getDatabaseExecuteSuccessCount(), equalTo(2L));

    operator.setJdbcOperations(new JdbcOperationsAdapter());
    try {
      operator.execute(new Object[]{user}, stat);
    } catch (UnsupportedOperationException e) {
    }
    assertThat(stat.getDatabaseExecuteExceptionCount(), equalTo(1L));
    try {
      operator.execute(new Object[]{user}, stat);
    } catch (UnsupportedOperationException e) {
    }
    assertThat(stat.getDatabaseExecuteExceptionCount(), equalTo(2L));
  }

  private AbstractOperator getOperator(TypeToken<?> pt, TypeToken<?> rt, String srcSql, List<Annotation> annos)
      throws Exception {
    List<Annotation> empty = Collections.emptyList();
    ParameterDescriptor p = ParameterDescriptor.create(0, pt.getType(), empty, "1");
    List<ParameterDescriptor> pds = Arrays.asList(p);

    List<Annotation> methodAnnos = new ArrayList<Annotation>();
    methodAnnos.add(new MockDB());
    methodAnnos.add(new MockSQL(srcSql));
    for (Annotation anno : annos) {
      methodAnnos.add(anno);
    }
    ReturnDescriptor rd = ReturnDescriptor.create(rt.getType(), methodAnnos);
    MethodDescriptor md = MethodDescriptor.create(null, null, rd, pds);
    DataSourceFactoryGroup group = new DataSourceFactoryGroup();
    group.addDataSourceFactory(new SimpleDataSourceFactory(DataSourceConfig.getDataSource()));

    OperatorFactory factory = new OperatorFactory(group, null, new InterceptorChain(), new Config());

    AbstractOperator operator = factory.getOperator(md, MetaStat.create());
    return operator;
  }

}
