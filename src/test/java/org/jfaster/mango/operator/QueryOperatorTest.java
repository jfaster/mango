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

import org.jfaster.mango.datasource.factory.SimpleDataSourceFactory;
import org.jfaster.mango.mapper.RowMapper;
import org.jfaster.mango.reflect.MethodDescriptor;
import org.jfaster.mango.reflect.ParameterDescriptor;
import org.jfaster.mango.reflect.ReturnDescriptor;
import org.jfaster.mango.reflect.TypeToken;
import org.jfaster.mango.support.Config;
import org.jfaster.mango.support.JdbcOperationsAdapter;
import org.jfaster.mango.support.MockDB;
import org.jfaster.mango.support.MockSQL;
import org.jfaster.mango.support.model4table.User;
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
        Operator operator = getOperator(t, t, srcSql);

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public <T> T queryForObject(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
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
        operator.execute(new Object[]{user});
    }

    @Test
    public void testQueryList() throws Exception {
        TypeToken<User> pt = TypeToken.of(User.class);
        TypeToken<List<User>> rt = new TypeToken<List<User>>() {};
        String srcSql = "select * from user where id=:1.id and name=:1.name";
        Operator operator = getOperator(pt, rt, srcSql);

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public <T> List<T> queryForList(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
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
        operator.execute(new Object[]{user});
    }

    @Test
    public void testQuerySet() throws Exception {
        TypeToken<User> pt = TypeToken.of(User.class);
        TypeToken<Set<User>> rt = new TypeToken<Set<User>>() {};
        String srcSql = "select * from user where id=:1.id and name=:1.name";
        Operator operator = getOperator(pt, rt, srcSql);

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public <T> Set<T> queryForSet(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
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
        operator.execute(new Object[]{user});
    }

    @Test
    public void testQueryArray() throws Exception {
        TypeToken<User> pt = TypeToken.of(User.class);
        TypeToken<User[]> rt = new TypeToken<User[]>() {};
        String srcSql = "select * from user where id=:1.id and name=:1.name";
        Operator operator = getOperator(pt, rt, srcSql);

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public <T> Object queryForArray(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
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
        operator.execute(new Object[]{user});
    }

    @Test
    public void testQueryIn() throws Exception {
        TypeToken<List<Integer>> pt = new TypeToken<List<Integer>>() {};
        TypeToken<List<User>> rt = new TypeToken<List<User>>() {};
        String srcSql = "select * from user where id in (:1)";
        Operator operator = getOperator(pt, rt, srcSql);

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public <T> List<T> queryForList(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
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
        operator.execute(new Object[]{ids});
    }

    @Test
    public void testQueryInCount() throws Exception {
        TypeToken<List<Integer>> pt = new TypeToken<List<Integer>>() {};
        TypeToken<Integer> rt = new TypeToken<Integer>() {};
        String srcSql = "select count(1) from user where id in (:1)";
        Operator operator = getOperator(pt, rt, srcSql);

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @SuppressWarnings("unchecked")
            @Override
            public <T> T queryForObject(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
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
        Integer r = (Integer) operator.execute(new Object[]{ids});
        assertThat(r, is(3));
    }

    @Test
    public void testStatsCounter() throws Exception {
        TypeToken<User> t = TypeToken.of(User.class);
        String srcSql = "select * from user where id=:1.id and name=:1.name";
        Operator operator = getOperator(t, t, srcSql);

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        User user = new User();
        user.setId(100);
        user.setName("ash");

        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public <T> T queryForObject(DataSource ds, String sql, Object[] args, RowMapper<T> rowMapper) {
                return null;
            }
        });
        operator.execute(new Object[]{user});
        assertThat(sc.snapshot().getExecuteSuccessCount(), equalTo(1L));
        operator.execute(new Object[]{user});
        assertThat(sc.snapshot().getExecuteSuccessCount(), equalTo(2L));

        operator.setJdbcOperations(new JdbcOperationsAdapter());
        try {
            operator.execute(new Object[]{user});
        } catch (UnsupportedOperationException e) {
        }
        assertThat(sc.snapshot().getExecuteExceptionCount(), equalTo(1L));
        try {
            operator.execute(new Object[]{user});
        } catch (UnsupportedOperationException e) {
        }
        assertThat(sc.snapshot().getExecuteExceptionCount(), equalTo(2L));
    }

    private Operator getOperator(TypeToken<?> pt, TypeToken<?> rt, String srcSql) throws Exception {
        List<Annotation> empty = Collections.emptyList();
        ParameterDescriptor p = new ParameterDescriptor(0, pt.getType(), empty, "1");
        List<ParameterDescriptor> pds = Arrays.asList(p);

        List<Annotation> methodAnnos = new ArrayList<Annotation>();
        methodAnnos.add(new MockDB());
        methodAnnos.add(new MockSQL(srcSql));
        ReturnDescriptor rd = new ReturnDescriptor(rt.getType(), methodAnnos);
        MethodDescriptor md = new MethodDescriptor(rd, pds);

        OperatorFactory factory = new OperatorFactory(
                new SimpleDataSourceFactory(Config.getDataSource()), null, new InterceptorChain());

        Operator operator = factory.getOperator(md);
        return operator;
    }

}
