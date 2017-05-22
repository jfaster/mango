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
import org.jfaster.mango.exception.DescriptionException;
import org.jfaster.mango.interceptor.InterceptorChain;
import org.jfaster.mango.jdbc.exception.DataAccessException;
import org.jfaster.mango.sharding.DatabaseShardingStrategy;
import org.jfaster.mango.sharding.ModHundredTableShardingStrategy;
import org.jfaster.mango.stat.MetaStat;
import org.jfaster.mango.stat.InvocationStat;
import org.jfaster.mango.support.*;
import org.jfaster.mango.support.model4table.User;
import org.jfaster.mango.util.reflect.TypeToken;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author ash
 */
public class BatchUpdateOperatorTest {

  @Test
  public void testExecuteReturnVoid() throws Exception {
    TypeToken<List<User>> pt = new TypeToken<List<User>>() {
    };
    TypeToken<Void> rt = TypeToken.of(void.class);
    String srcSql = "update user set name=:1.name where id=:1.id";
    AbstractOperator operator = getOperator(pt, rt, srcSql);

    final int[] expectedInts = new int[]{1, 2};
    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @Override
      public int[] batchUpdate(DataSource ds, List<BoundSql> boundSqls) {
        String sql = boundSqls.get(0).getSql();
        String descSql = "update user set name=? where id=?";
        assertThat(sql, equalTo(descSql));
        assertThat(boundSqls.size(), equalTo(2));
        assertThat(boundSqls.get(0).getArgs().get(0), equalTo((Object) "ash"));
        assertThat(boundSqls.get(0).getArgs().get(1), equalTo((Object) 100));
        assertThat(boundSqls.get(1).getArgs().get(0), equalTo((Object) "lucy"));
        assertThat(boundSqls.get(1).getArgs().get(1), equalTo((Object) 200));
        return expectedInts;
      }
    });

    List<User> users = Arrays.asList(new User(100, "ash"), new User(200, "lucy"));
    Object actual = operator.execute(new Object[]{users}, InvocationStat.create());
    assertThat(actual, nullValue());
  }

  @Test
  public void testExecuteReturnInt() throws Exception {
    TypeToken<List<User>> pt = new TypeToken<List<User>>() {
    };
    TypeToken<Integer> rt = TypeToken.of(int.class);
    String srcSql = "update user set name=:1.name where id=:1.id";
    AbstractOperator operator = getOperator(pt, rt, srcSql);

    final int[] expectedInts = new int[]{1, 2};
    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @Override
      public int[] batchUpdate(DataSource ds, List<BoundSql> boundSqls) {
        String sql = boundSqls.get(0).getSql();
        String descSql = "update user set name=? where id=?";
        assertThat(sql, equalTo(descSql));
        assertThat(boundSqls.size(), equalTo(2));
        assertThat(boundSqls.get(0).getArgs().get(0), equalTo((Object) "ash"));
        assertThat(boundSqls.get(0).getArgs().get(1), equalTo((Object) 100));
        assertThat(boundSqls.get(1).getArgs().get(0), equalTo((Object) "lucy"));
        assertThat(boundSqls.get(1).getArgs().get(1), equalTo((Object) 200));
        return expectedInts;
      }
    });

    List<User> users = Arrays.asList(new User(100, "ash"), new User(200, "lucy"));
    int actual = (Integer) operator.execute(new Object[]{users}, InvocationStat.create());
    assertThat(actual, is(3));
  }

  @Test
  public void testExecuteReturnIntArray() throws Exception {
    TypeToken<List<User>> pt = new TypeToken<List<User>>() {
    };
    TypeToken<int[]> rt = TypeToken.of(int[].class);
    String srcSql = "update user set name=:1.name where id=:1.id";
    AbstractOperator operator = getOperator(pt, rt, srcSql);

    final int[] expectedInts = new int[]{1, 2};
    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @Override
      public int[] batchUpdate(DataSource ds, List<BoundSql> boundSqls) {
        String sql = boundSqls.get(0).getSql();
        String descSql = "update user set name=? where id=?";
        assertThat(sql, equalTo(descSql));
        assertThat(boundSqls.size(), equalTo(2));
        assertThat(boundSqls.get(0).getArgs().get(0), equalTo((Object) "ash"));
        assertThat(boundSqls.get(0).getArgs().get(1), equalTo((Object) 100));
        assertThat(boundSqls.get(1).getArgs().get(0), equalTo((Object) "lucy"));
        assertThat(boundSqls.get(1).getArgs().get(1), equalTo((Object) 200));
        return expectedInts;
      }
    });

    List<User> users = Arrays.asList(new User(100, "ash"), new User(200, "lucy"));
    int[] actualInts = (int[]) operator.execute(new Object[]{users}, InvocationStat.create());
    assertThat(Arrays.toString(actualInts), equalTo(Arrays.toString(expectedInts)));
  }

  @Test
  public void testExecuteReturnIntegerArray() throws Exception {
    TypeToken<List<User>> pt = new TypeToken<List<User>>() {
    };
    TypeToken<Integer[]> rt = TypeToken.of(Integer[].class);
    String srcSql = "update user set name=:1.name where id=:1.id";
    AbstractOperator operator = getOperator(pt, rt, srcSql);

    final int[] expectedInts = new int[]{1, 2};
    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @Override
      public int[] batchUpdate(DataSource ds, List<BoundSql> boundSqls) {
        String sql = boundSqls.get(0).getSql();
        String descSql = "update user set name=? where id=?";
        assertThat(sql, equalTo(descSql));
        assertThat(boundSqls.size(), equalTo(2));
        assertThat(boundSqls.get(0).getArgs().get(0), equalTo((Object) "ash"));
        assertThat(boundSqls.get(0).getArgs().get(1), equalTo((Object) 100));
        assertThat(boundSqls.get(1).getArgs().get(0), equalTo((Object) "lucy"));
        assertThat(boundSqls.get(1).getArgs().get(1), equalTo((Object) 200));
        return expectedInts;
      }
    });

    List<User> users = Arrays.asList(new User(100, "ash"), new User(200, "lucy"));
    Integer[] actualInts = (Integer[]) operator.execute(new Object[]{users}, InvocationStat.create());
    assertThat(Arrays.toString(actualInts), equalTo(Arrays.toString(expectedInts)));
  }


  @Test
  public void testExecuteMulti() throws Exception {
    TypeToken<List<User>> pt = new TypeToken<List<User>>() {
    };
    TypeToken<int[]> rt = TypeToken.of(int[].class);
    String srcSql = "update #table set name=:1.name where id=:1.id";
    AbstractOperator operator = getOperator2(pt, rt, srcSql);

    operator.setJdbcOperations(new JdbcOperationsAdapter() {

      @Override
      public int[] batchUpdate(DataSource ds, List<BoundSql> boundSqls) throws DataAccessException {
        if (boundSqls.size() == 3) {
          List<String> descSqls = Arrays.asList(
              "update user_30 set name=? where id=?",
              "update user_10 set name=? where id=?",
              "update user_20 set name=? where id=?");
          List<String> sqls = new ArrayList<String>();
          for (BoundSql boundSql : boundSqls) {
            sqls.add(boundSql.getSql());
          }
          assertThat(sqls, equalTo(descSqls));
          assertThat(boundSqls.size(), equalTo(3));
          assertThat(boundSqls.get(0).getArgs().get(0), equalTo((Object) "ash"));
          assertThat(boundSqls.get(0).getArgs().get(1), equalTo((Object) 30));
          assertThat(boundSqls.get(1).getArgs().get(0), equalTo((Object) "lily"));
          assertThat(boundSqls.get(1).getArgs().get(1), equalTo((Object) 10));
          assertThat(boundSqls.get(2).getArgs().get(0), equalTo((Object) "gill"));
          assertThat(boundSqls.get(2).getArgs().get(1), equalTo((Object) 20));
          return new int[] {3, 1, 2};
        } else if (boundSqls.size() == 2) {
          List<String> descSqls = Arrays.asList(
              "update user_60 set name=? where id=?",
              "update user_55 set name=? where id=?");
          List<String> sqls = new ArrayList<String>();
          for (BoundSql boundSql : boundSqls) {
            sqls.add(boundSql.getSql());
          }
          assertThat(sqls, equalTo(descSqls));
          assertThat(boundSqls.size(), equalTo(2));
          assertThat(boundSqls.get(0).getArgs().get(0), equalTo((Object) "lucy"));
          assertThat(boundSqls.get(0).getArgs().get(1), equalTo((Object) 60));
          assertThat(boundSqls.get(1).getArgs().get(0), equalTo((Object) "liu"));
          assertThat(boundSqls.get(1).getArgs().get(1), equalTo((Object) 55));
          return new int[] {6, 5};
        } else {
          throw new IllegalStateException();
        }
      }

    });

    List<User> users = Arrays.asList(
        new User(30, "ash"), new User(60, "lucy"), new User(10, "lily"),
        new User(20, "gill"), new User(55, "liu"));
    int[] actualInts = (int[]) operator.execute(new Object[]{users}, InvocationStat.create());
    assertThat(Arrays.toString(actualInts), equalTo(Arrays.toString(new int[]{3, 6, 1, 2, 5})));
  }

  @Test
  public void testStatsCounter() throws Exception {
    TypeToken<List<User>> pt = new TypeToken<List<User>>() {
    };
    TypeToken<int[]> rt = TypeToken.of(int[].class);
    String srcSql = "update user set name=:1.name where id=:1.id";
    AbstractOperator operator = getOperator(pt, rt, srcSql);

    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @Override
      public int[] batchUpdate(DataSource ds, List<BoundSql> boundSqls) {
        String descSql = "update user set name=? where id=?";
        assertThat(boundSqls.get(0).getSql(), equalTo(descSql));
        assertThat(boundSqls.size(), equalTo(2));
        assertThat(boundSqls.get(0).getArgs().get(0), equalTo((Object) "ash"));
        assertThat(boundSqls.get(0).getArgs().get(1), equalTo((Object) 100));
        assertThat(boundSqls.get(1).getArgs().get(0), equalTo((Object) "lucy"));
        assertThat(boundSqls.get(1).getArgs().get(1), equalTo((Object) 200));
        return new int[]{9, 7};
      }
    });
    List<User> users = Arrays.asList(new User(100, "ash"), new User(200, "lucy"));
    InvocationStat stat = InvocationStat.create();
    operator.execute(new Object[]{users}, stat);
    assertThat(stat.getDatabaseExecuteSuccessCount(), equalTo(1L));
    operator.execute(new Object[]{users}, stat);
    assertThat(stat.getDatabaseExecuteSuccessCount(), equalTo(2L));

    operator.setJdbcOperations(new JdbcOperationsAdapter());
    try {
      operator.execute(new Object[]{users}, stat);
    } catch (UnsupportedOperationException e) {
    }
    assertThat(stat.getDatabaseExecuteExceptionCount(), equalTo(1L));
    try {
      operator.execute(new Object[]{users}, stat);
    } catch (UnsupportedOperationException e) {
    }
    assertThat(stat.getDatabaseExecuteExceptionCount(), equalTo(2L));
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testExecuteReturnTypeError() throws Exception {
    thrown.expect(DescriptionException.class);
    thrown.expectMessage("the return type of batch update expected one of " +
        "[void, int, int[], Void, Integer, Integer[]] but class java.lang.String");

    TypeToken<List<User>> pt = new TypeToken<List<User>>() {
    };
    TypeToken<String> rt = TypeToken.of(String.class);
    String srcSql = "update user set name=:1.name where id=:1.id";
    AbstractOperator operator = getOperator(pt, rt, srcSql);

    final int[] expectedInts = new int[]{1, 2};
    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @Override
      public int[] batchUpdate(DataSource ds, List<BoundSql> boundSqls) {
        String descSql = "update user set name=? where id=?";
        assertThat(boundSqls.get(0).getSql(), equalTo(descSql));
        assertThat(boundSqls.size(), equalTo(2));
        assertThat(boundSqls.get(0).getArgs().get(0), equalTo((Object) "ash"));
        assertThat(boundSqls.get(0).getArgs().get(1), equalTo((Object) 100));
        assertThat(boundSqls.get(1).getArgs().get(0), equalTo((Object) "lucy"));
        assertThat(boundSqls.get(1).getArgs().get(1), equalTo((Object) 200));
        return expectedInts;
      }
    });

    List<User> users = Arrays.asList(new User(100, "ash"), new User(200, "lucy"));
    operator.execute(new Object[]{users}, InvocationStat.create());
  }

  private AbstractOperator getOperator(TypeToken<?> pt, TypeToken<?> rt, String srcSql) throws Exception {
    List<Annotation> empty = Collections.emptyList();
    ParameterDescriptor p = ParameterDescriptor.create(0, pt.getType(), empty, "1");
    List<ParameterDescriptor> pds = Arrays.asList(p);

    List<Annotation> methodAnnos = new ArrayList<Annotation>();
    methodAnnos.add(new MockDB());
    methodAnnos.add(new MockSQL(srcSql));
    ReturnDescriptor rd = ReturnDescriptor.create(rt.getType(), methodAnnos);
    MethodDescriptor md = MethodDescriptor.create(null, null, rd, pds);

    DataSourceFactoryGroup group = new DataSourceFactoryGroup();
    group.addDataSourceFactory(new SimpleDataSourceFactory(DataSourceConfig.getDataSource()));
    OperatorFactory factory = new OperatorFactory(group, null, new InterceptorChain(), new Config());

    AbstractOperator operator = factory.getOperator(md, MetaStat.create());
    return operator;
  }

  private AbstractOperator getOperator2(TypeToken<?> pt, TypeToken<?> rt, String srcSql) throws Exception {
    List<Annotation> pAnnos = new ArrayList<Annotation>();
    pAnnos.add(new MockShardingBy("id"));
    ParameterDescriptor p = ParameterDescriptor.create(0, pt.getType(), pAnnos, "1");
    List<ParameterDescriptor> pds = Arrays.asList(p);

    List<Annotation> methodAnnos = new ArrayList<Annotation>();
    methodAnnos.add(new MockDB("", "user"));
    methodAnnos.add(new MockSharding(ModHundredTableShardingStrategy.class, MyDatabaseShardingStrategy.class, null));
    methodAnnos.add(new MockSQL(srcSql));
    ReturnDescriptor rd = ReturnDescriptor.create(rt.getType(), methodAnnos);
    MethodDescriptor md = MethodDescriptor.create(null, null, rd, pds);

    DataSourceFactoryGroup group = new DataSourceFactoryGroup();
    group.addDataSourceFactory(new SimpleDataSourceFactory("l50", DataSourceConfig.getDataSource(0)));
    group.addDataSourceFactory(new SimpleDataSourceFactory("g50", DataSourceConfig.getDataSource(1)));
    OperatorFactory factory = new OperatorFactory(group, null, new InterceptorChain(), new Config());
    AbstractOperator operator = factory.getOperator(md, MetaStat.create());
    return operator;
  }

  public static class MyDatabaseShardingStrategy implements DatabaseShardingStrategy {

    @Override
    public String getDataSourceFactoryName(Object shardParam) {
      Integer i = (Integer) shardParam;
      if (i < 50) {
        return "l50";
      } else {
        return "g50";
      }
    }
  }

}
