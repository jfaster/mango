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

import org.jfaster.mango.cache.CacheHandler;
import org.jfaster.mango.cache.Day;
import org.jfaster.mango.datasource.factory.SimpleDataSourceFactory;
import org.jfaster.mango.mapper.RowMapper;
import org.jfaster.mango.reflect.MethodDescriptor;
import org.jfaster.mango.reflect.ParameterDescriptor;
import org.jfaster.mango.reflect.ReturnDescriptor;
import org.jfaster.mango.reflect.TypeToken;
import org.jfaster.mango.support.*;
import org.jfaster.mango.support.model4table.User;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

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
        Operator operator = getOperator(pt, rt, srcSql, new CacheHandlerAdapter() {
            @Override
            public Object get(String key) {
                assertThat(key, equalTo("user_1"));
                return new User();
            }
        }, new MockCacheBy(""));

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter());

        operator.execute(new Object[]{1});
        assertThat(sc.snapshot().getHitCount(), equalTo(1L));
    }

    @Test
    public void testQuerySingleKeyMiss() throws Exception {
        TypeToken<Integer> pt = TypeToken.of(Integer.class);
        TypeToken<User> rt = TypeToken.of(User.class);
        String srcSql = "select * from user where id=:1";
        Operator operator = getOperator(pt, rt, srcSql, new CacheHandlerAdapter() {
            @Override
            public Object get(String key) {
                assertThat(key, equalTo("user_1"));
                return null;
            }

            @Override
            public void set(String key, Object value, int expires) {
                assertThat(key, equalTo("user_1"));
                assertThat(expires, equalTo((int) TimeUnit.DAYS.toSeconds(1)));
            }
        }, new MockCacheBy(""));

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {

            @Override
            public <T> T queryForObject(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
                String descSql = "select * from user where id=?";
                assertThat(sql, equalTo(descSql));
                assertThat(args.length, equalTo(1));
                assertThat(args[0], equalTo((Object) 1));
                return (T) new User();
            }
        });

        operator.execute(new Object[]{1});
        assertThat(sc.snapshot().getMissCount(), equalTo(1L));
    }

    @Test
    public void testQueryMultiKeyAllHit() throws Exception {
        TypeToken<List<Integer>> pt = new TypeToken<List<Integer>>() {};
        TypeToken<List<User>> rt = new TypeToken<List<User>>() {};
        String srcSql = "select * from user where id in (:1)";
        Operator operator = getOperator(pt, rt, srcSql, new CacheHandlerAdapter() {
            @Override
            public Map<String, Object> getBulk(Set<String> keys) {
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("user_1", new User());
                map.put("user_2", new User());
                map.put("user_3", new User());
                assertThat(keys, equalTo(map.keySet()));
                return map;
            }
        }, new MockCacheBy(""));

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter());

        operator.execute(new Object[]{Arrays.asList(1, 2, 3)});
        assertThat(sc.snapshot().getHitCount(), equalTo(3L));
    }

    @Test
    public void testQueryMultiKeyAllMiss() throws Exception {
        TypeToken<List<Integer>> pt = new TypeToken<List<Integer>>() {};
        TypeToken<List<User>> rt = new TypeToken<List<User>>() {};
        String srcSql = "select * from user where id in (:1)";
        final Set<String> keys = new HashSet<String>();
        final Set<String> setKeys = new HashSet<String>();
        keys.add("user_1");
        keys.add("user_2");
        keys.add("user_3");
        Operator operator = getOperator(pt, rt, srcSql, new CacheHandlerAdapter() {
            @Override
            public Map<String, Object> getBulk(Set<String> keys) {
                assertThat(keys, equalTo(keys));
                return null;
            }
            @Override
            public void set(String key, Object value, int expires) {
                setKeys.add(key);
            }
        }, new MockCacheBy(""));

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public <T> List<T> queryForList(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
                String descSql = "select * from user where id in (?,?,?)";
                assertThat(sql, equalTo(descSql));
                assertThat(args.length, equalTo(3));
                assertThat(args[0], equalTo((Object) 1));
                assertThat(args[1], equalTo((Object) 2));
                assertThat(args[2], equalTo((Object) 3));

                List<T> users = new ArrayList<T>();
                users.add((T) new User(1, "1"));
                users.add((T) new User(2, "2"));
                users.add((T) new User(3, "3"));
                return users;
            }
        });

        operator.execute(new Object[]{Arrays.asList(1, 2, 3)});
        assertThat(sc.snapshot().getMissCount(), equalTo(3L));
        assertThat(keys, equalTo(setKeys));
    }

    @Test
    public void testQueryMultiKey() throws Exception {
        TypeToken<List<Integer>> pt = new TypeToken<List<Integer>>() {};
        TypeToken<List<User>> rt = new TypeToken<List<User>>() {};
        String srcSql = "select * from user where id in (:1)";
        final Set<String> keys = new HashSet<String>();
        final Set<String> setKeys = new HashSet<String>();
        keys.add("user_1");
        keys.add("user_2");
        keys.add("user_3");
        Operator operator = getOperator(pt, rt, srcSql, new CacheHandlerAdapter() {
            @Override
            public Map<String, Object> getBulk(Set<String> keys) {
                assertThat(keys, equalTo(keys));
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("user_2", new User());
                return map;
            }
            @Override
            public void set(String key, Object value, int expires) {
                setKeys.add(key);
            }
        }, new MockCacheBy(""));

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public <T> List<T> queryForList(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
                String descSql = "select * from user where id in (?,?)";
                assertThat(sql, equalTo(descSql));
                assertThat(args.length, equalTo(2));
                assertThat(args[0], equalTo((Object) 1));
                assertThat(args[1], equalTo((Object) 3));

                List<T> users = new ArrayList<T>();
                users.add((T) new User(1, "1"));
                users.add((T) new User(3, "3"));
                return users;
            }
        });

        operator.execute(new Object[]{Arrays.asList(1, 2, 3)});
        assertThat(sc.snapshot().getHitCount(), equalTo(1L));
        assertThat(sc.snapshot().getMissCount(), equalTo(2L));
        keys.remove("user_2");
        assertThat(keys, equalTo(setKeys));
    }


    private Operator getOperator(TypeToken<?> pt, TypeToken<?> rt, String srcSql,
                                 CacheHandler ch, MockCacheBy cacheBy) throws Exception {
        List<Annotation> pAnnos = new ArrayList<Annotation>();
        pAnnos.add(cacheBy);
        ParameterDescriptor p = new ParameterDescriptor(0, pt.getType(), pAnnos, "1");
        List<ParameterDescriptor> pds = Arrays.asList(p);

        List<Annotation> methodAnnos = new ArrayList<Annotation>();
        methodAnnos.add(new MockDB());
        methodAnnos.add(new MockCache("user", Day.class));
        methodAnnos.add(new MockSQL(srcSql));
        ReturnDescriptor rd = new ReturnDescriptor(rt.getType(), methodAnnos);
        MethodDescriptor md = new MethodDescriptor(rd, pds);

        OperatorFactory factory = new OperatorFactory(
                new SimpleDataSourceFactory(Config.getDataSource()), ch, new InterceptorChain());

        Operator operator = factory.getOperator(md);
        return operator;
    }

}
