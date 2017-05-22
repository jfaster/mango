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

package org.jfaster.mango.operator.cache;

import org.hamcrest.Matchers;
import org.jfaster.mango.binding.BoundSql;
import org.jfaster.mango.datasource.DataSourceFactoryGroup;
import org.jfaster.mango.datasource.SimpleDataSourceFactory;
import org.jfaster.mango.descriptor.MethodDescriptor;
import org.jfaster.mango.descriptor.ParameterDescriptor;
import org.jfaster.mango.descriptor.ReturnDescriptor;
import org.jfaster.mango.interceptor.InterceptorChain;
import org.jfaster.mango.jdbc.ListSupplier;
import org.jfaster.mango.mapper.RowMapper;
import org.jfaster.mango.operator.AbstractOperator;
import org.jfaster.mango.operator.Config;
import org.jfaster.mango.operator.Operator;
import org.jfaster.mango.operator.OperatorFactory;
import org.jfaster.mango.stat.MetaStat;
import org.jfaster.mango.stat.InvocationStat;
import org.jfaster.mango.support.*;
import org.jfaster.mango.support.model4table.User;
import org.jfaster.mango.util.reflect.TypeToken;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
@SuppressWarnings("unchecked")
public class CacheableQueryOperatorTest {

  @Test
  public void testQuerySingleKeyHit() throws Exception {
    TypeToken<Integer> pt = TypeToken.of(Integer.class);
    TypeToken<User> rt = TypeToken.of(User.class);
    String srcSql = "select * from user where id=:1";

    AbstractOperator operator = getOperator(pt, rt, srcSql, new CacheHandlerAdapter() {
      @Override
      public Object get(String key, Type type, Class<?> daoClass) {
        assertThat(key, Matchers.equalTo("user_1"));
        return new User();
      }
    }, new MockCacheBy(""));

    operator.setJdbcOperations(new JdbcOperationsAdapter());

    InvocationStat stat = InvocationStat.create();
    operator.execute(new Object[]{1}, stat);
    assertThat(stat.getHitCount(), Matchers.equalTo(1L));
  }

  @Test
  public void testQuerySingleKeyMiss() throws Exception {
    TypeToken<Integer> pt = TypeToken.of(Integer.class);
    TypeToken<User> rt = TypeToken.of(User.class);
    String srcSql = "select * from user where id=:1";

    AbstractOperator operator = getOperator(pt, rt, srcSql, new CacheHandlerAdapter() {
      @Override
      public Object get(String key, Type type, Class<?> daoClass) {
        assertThat(key, Matchers.equalTo("user_1"));
        return null;
      }

      @Override
      public void set(String key, Object value, int expires, Class<?> daoClass) {
        assertThat(key, Matchers.equalTo("user_1"));
        assertThat(expires, Matchers.equalTo((int) TimeUnit.DAYS.toSeconds(1)));
      }
    }, new MockCacheBy(""));

    operator.setJdbcOperations(new JdbcOperationsAdapter() {

      @Override
      public <T> T queryForObject(DataSource ds, BoundSql boundSql, RowMapper<T> rowMapper) {
        String sql = boundSql.getSql();
        Object[] args = boundSql.getArgs().toArray();
        String descSql = "select * from user where id=?";
        assertThat(sql, Matchers.equalTo(descSql));
        assertThat(args.length, Matchers.equalTo(1));
        assertThat(args[0], Matchers.equalTo((Object) 1));
        return (T) new User();
      }
    });

    InvocationStat stat = InvocationStat.create();
    operator.execute(new Object[]{1}, stat);
    assertThat(stat.getMissCount(), Matchers.equalTo(1L));
  }

  @Test
  public void testQueryMultiKeyAllHit() throws Exception {
    TypeToken<List<Integer>> pt = new TypeToken<List<Integer>>() {
    };
    TypeToken<List<User>> rt = new TypeToken<List<User>>() {
    };
    String srcSql = "select * from user where id in (:1)";

    AbstractOperator operator = getOperator(pt, rt, srcSql, new CacheHandlerAdapter() {
      @Override
      public Map<String, Object> getBulk(Set<String> keys, Type type, Class<?> daoClass) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("user_1", new User());
        map.put("user_2", new User());
        map.put("user_3", new User());
        assertThat(keys, Matchers.equalTo(map.keySet()));
        return map;
      }
    }, new MockCacheBy(""));

    operator.setJdbcOperations(new JdbcOperationsAdapter());

    InvocationStat stat = InvocationStat.create();
    operator.execute(new Object[]{Arrays.asList(1, 2, 3)}, stat);
    assertThat(stat.getHitCount(), equalTo(3L));
    assertThat(((CacheableQueryOperator) operator).propertyOfMapperInvoker.getName(), equalTo("id"));
  }

  @Test
  public void testQueryMultiKeyAllMiss() throws Exception {
    TypeToken<List<Integer>> pt = new TypeToken<List<Integer>>() {
    };
    TypeToken<List<User>> rt = new TypeToken<List<User>>() {
    };
    String srcSql = "select * from user where id in (:1)";
    final Set<String> keys = new HashSet<String>();
    final Set<String> setKeys = new HashSet<String>();
    keys.add("user_1");
    keys.add("user_2");
    keys.add("user_3");

    AbstractOperator operator = getOperator(pt, rt, srcSql, new CacheHandlerAdapter() {
      @Override
      public Map<String, Object> getBulk(Set<String> keys, Type type, Class<?> daoClass) {
        assertThat(keys, Matchers.equalTo(keys));
        return null;
      }

      @Override
      public void set(String key, Object value, int expires, Class<?> daoClass) {
        setKeys.add(key);
      }
    }, new MockCacheBy(""));

    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @Override
      public <T> List<T> queryForList(DataSource ds, BoundSql boundSql,
                                      ListSupplier listSupplier, RowMapper<T> rowMapper) {
        String sql = boundSql.getSql();
        Object[] args = boundSql.getArgs().toArray();
        String descSql = "select * from user where id in (?,?,?)";
        assertThat(sql, Matchers.equalTo(descSql));
        assertThat(args.length, Matchers.equalTo(3));
        assertThat(args[0], Matchers.equalTo((Object) 1));
        assertThat(args[1], Matchers.equalTo((Object) 2));
        assertThat(args[2], Matchers.equalTo((Object) 3));

        List<T> users = new ArrayList<T>();
        users.add((T) new User(1, "1"));
        users.add((T) new User(2, "2"));
        users.add((T) new User(3, "3"));
        return users;
      }
    });

    InvocationStat stat = InvocationStat.create();
    operator.execute(new Object[]{Arrays.asList(1, 2, 3)}, stat);
    assertThat(stat.getMissCount(), Matchers.equalTo(3L));
    assertThat(keys, Matchers.equalTo(setKeys));
  }

  @Test
  public void testQueryMultiKey() throws Exception {
    TypeToken<List<Integer>> pt = new TypeToken<List<Integer>>() {
    };
    TypeToken<List<User>> rt = new TypeToken<List<User>>() {
    };
    String srcSql = "select * from user where id in (:1)";
    final Set<String> keys = new HashSet<String>();
    final Set<String> setKeys = new HashSet<String>();
    keys.add("user_1");
    keys.add("user_2");
    keys.add("user_3");

    AbstractOperator operator = getOperator(pt, rt, srcSql, new CacheHandlerAdapter() {
      @Override
      public Map<String, Object> getBulk(Set<String> keys, Type type, Class<?> daoClass) {
        assertThat(keys, Matchers.equalTo(keys));
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("user_2", new User());
        return map;
      }

      @Override
      public void set(String key, Object value, int expires, Class<?> daoClass) {
        setKeys.add(key);
      }
    }, new MockCacheBy(""));

    operator.setJdbcOperations(new JdbcOperationsAdapter() {
      @Override
      public <T> List<T> queryForList(DataSource ds, BoundSql boundSql,
                                      ListSupplier listSupplier, RowMapper<T> rowMapper) {
        String sql = boundSql.getSql();
        Object[] args = boundSql.getArgs().toArray();
        String descSql = "select * from user where id in (?,?)";
        assertThat(sql, Matchers.equalTo(descSql));
        assertThat(args.length, Matchers.equalTo(2));
        assertThat(args[0], Matchers.equalTo((Object) 1));
        assertThat(args[1], Matchers.equalTo((Object) 3));

        List<T> users = new ArrayList<T>();
        users.add((T) new User(1, "1"));
        users.add((T) new User(3, "3"));
        return users;
      }
    });

    InvocationStat stat = InvocationStat.create();
    operator.execute(new Object[]{Arrays.asList(1, 2, 3)}, stat);
    assertThat(stat.getHitCount(), Matchers.equalTo(1L));
    assertThat(stat.getMissCount(), Matchers.equalTo(2L));
    keys.remove("user_2");
    assertThat(keys, Matchers.equalTo(setKeys));
  }

  @Test
  public void testQueryMultiKeyPropertyOfMapper() throws Exception {
    TypeToken<List<Integer>> pt = new TypeToken<List<Integer>>() {
    };
    TypeToken<List<X>> rt = new TypeToken<List<X>>() {
    };
    String srcSql = "select * from user where msg_id in (:1)";
    AbstractOperator operator = getOperator(pt, rt, srcSql, new CacheHandlerAdapter() {
    }, new MockCacheBy(""));
    assertThat(((CacheableQueryOperator) operator).propertyOfMapperInvoker.getName(), equalTo("msgId"));
  }

  private static class X {

    private int msgId;
    private String content;

    public int getMsgId() {
      return msgId;
    }

    public void setMsgId(int msgId) {
      this.msgId = msgId;
    }

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }
  }


  private AbstractOperator getOperator(TypeToken<?> pt, TypeToken<?> rt, String srcSql,
                                       CacheHandler ch, MockCacheBy cacheBy) throws Exception {
    List<Annotation> pAnnos = new ArrayList<Annotation>();
    pAnnos.add(cacheBy);
    ParameterDescriptor p = ParameterDescriptor.create(0, pt.getType(), pAnnos, "1");
    List<ParameterDescriptor> pds = Arrays.asList(p);

    List<Annotation> methodAnnos = new ArrayList<Annotation>();
    methodAnnos.add(new MockDB());
    methodAnnos.add(new MockCache("user", Day.class));
    methodAnnos.add(new MockSQL(srcSql));
    ReturnDescriptor rd = ReturnDescriptor.create(rt.getType(), methodAnnos);
    MethodDescriptor md = MethodDescriptor.create(null, null, rd, pds);
    DataSourceFactoryGroup group = new DataSourceFactoryGroup();
    group.addDataSourceFactory(new SimpleDataSourceFactory(DataSourceConfig.getDataSource()));

    OperatorFactory factory = new OperatorFactory(group, ch, new InterceptorChain(), new Config());

    AbstractOperator operator = factory.getOperator(md, MetaStat.create());
    return operator;
  }

}
