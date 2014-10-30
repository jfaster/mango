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
import org.jfaster.mango.exception.IncorrectReturnTypeException;
import org.jfaster.mango.jdbc.RowMapper;
import org.jfaster.mango.support.Config;
import org.jfaster.mango.support.JdbcOperationsAdapter;
import org.jfaster.mango.support.MockDB;
import org.jfaster.mango.support.MockSQL;
import org.jfaster.mango.support.model4table.User;
import org.jfaster.mango.reflect.MethodDescriptor;
import org.jfaster.mango.reflect.ParameterDescriptor;
import org.jfaster.mango.reflect.TypeToken;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

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
        assertThat(sc.snapshot().executeSuccessCount(), equalTo(1L));
        operator.execute(new Object[]{user});
        assertThat(sc.snapshot().executeSuccessCount(), equalTo(2L));

        operator.setJdbcOperations(new JdbcOperationsAdapter());
        try {
            operator.execute(new Object[]{user});
        } catch (UnsupportedOperationException e) {
        }
        assertThat(sc.snapshot().executeExceptionCount(), equalTo(1L));
        try {
            operator.execute(new Object[]{user});
        } catch (UnsupportedOperationException e) {
        }
        assertThat(sc.snapshot().executeExceptionCount(), equalTo(2L));
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void testIncorrectReturnTypeException() throws Exception {
        thrown.expect(IncorrectReturnTypeException.class);
        thrown.expectMessage("if sql has in clause, return type expected array " +
                "or implementations of java.util.List or implementations of java.util.Set " +
                "but class org.jfaster.mango.support.model4table.User");

        TypeToken<List<Integer>> pt = new TypeToken<List<Integer>>() {};
        TypeToken<User> rt = TypeToken.of(User.class);
        String srcSql = "select * from user where id in (:1)";
        getOperator(pt, rt, srcSql);
    }

    private Operator getOperator(TypeToken<?> pt, TypeToken<?> rt, String srcSql) throws Exception {
        List<Annotation> empty = Collections.emptyList();
        ParameterDescriptor p = new ParameterDescriptor(0, pt.getType(), pt.getRawType(), empty, "1");
        List<ParameterDescriptor> pds = Arrays.asList(p);

        List<Annotation> methodAnnos = new ArrayList<Annotation>();
        methodAnnos.add(new MockDB());
        methodAnnos.add(new MockSQL(srcSql));
        MethodDescriptor md = new MethodDescriptor(rt.getType(), rt.getRawType(), methodAnnos, pds);

        OperatorFactory factory = new OperatorFactory(
                new SimpleDataSourceFactory(Config.getDataSource()),
                null, new InterceptorChain(), new InterceptorChain());

        Operator operator = factory.getOperator(md);
        return operator;
    }

}
