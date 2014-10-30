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

import org.jfaster.mango.datasource.factory.DataSourceFactory;
import org.jfaster.mango.datasource.factory.MultipleDataSourceFactory;
import org.jfaster.mango.datasource.factory.SimpleDataSourceFactory;
import org.jfaster.mango.datasource.router.DataSourceRouter;
import org.jfaster.mango.partition.ModHundredTablePartition;
import org.jfaster.mango.support.*;
import org.jfaster.mango.support.model4table.User;
import org.jfaster.mango.reflect.MethodDescriptor;
import org.jfaster.mango.reflect.ParameterDescriptor;
import org.jfaster.mango.reflect.TypeToken;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.*;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class BatchUpdateOperatorTest {

    @Test
    public void testExecute() throws Exception {
        TypeToken<List<User>> pt = new TypeToken<List<User>>() {};
        TypeToken<int[]> rt = TypeToken.of(int[].class);
        String srcSql = "update user set name=:1.name where id=:1.id";
        Operator operator = getOperator(pt, rt, srcSql);

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public int[] batchUpdate(DataSource ds, String sql, List<Object[]> batchArgs) {
                String descSql = "update user set name=? where id=?";
                assertThat(sql, equalTo(descSql));
                assertThat(batchArgs.size(), equalTo(2));
                assertThat(batchArgs.get(0)[0], equalTo((Object) "ash"));
                assertThat(batchArgs.get(0)[1], equalTo((Object) 100));
                assertThat(batchArgs.get(1)[0], equalTo((Object) "lucy"));
                assertThat(batchArgs.get(1)[1], equalTo((Object) 200));
                return new int[] {1};
            }
        });

        List<User> users = Arrays.asList(new User(100, "ash"), new User(200, "lucy"));
        operator.execute(new Object[]{users});
    }

    @Test
    public void testExecuteMulti() throws Exception {
        TypeToken<List<User>> pt = new TypeToken<List<User>>() {};
        TypeToken<int[]> rt = TypeToken.of(int[].class);
        String srcSql = "update #table set name=:1.name where id=:1.id";
        Operator operator = getOperator2(pt, rt, srcSql);

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public int[] batchUpdate(DataSource ds, List<String> sqls, List<Object[]> batchArgs) {
                List<String> descSqls = Arrays.asList("update user_10 set name=? where id=?",
                        "update user_20 set name=? where id=?");
                assertThat(sqls, equalTo(descSqls));
                assertThat(batchArgs.size(), equalTo(2));
                assertThat(batchArgs.get(0)[0], equalTo((Object) "ash"));
                assertThat(batchArgs.get(0)[1], equalTo((Object) 10));
                assertThat(batchArgs.get(1)[0], equalTo((Object) "lucy"));
                assertThat(batchArgs.get(1)[1], equalTo((Object) 20));
                return new int[] {1};
            }
            @Override
            public int[] batchUpdate(DataSource ds, String sql, List<Object[]> batchArgs) {
                String descSql = "update user_60 set name=? where id=?";
                assertThat(sql, equalTo(descSql));
                assertThat(batchArgs.size(), equalTo(1));
                assertThat(batchArgs.get(0)[0], equalTo((Object) "lily"));
                assertThat(batchArgs.get(0)[1], equalTo((Object) 60));
                return new int[] {1};
            }
        });

        List<User> users = Arrays.asList(new User(10, "ash"), new User(20, "lucy"), new User(60, "lily"));
        operator.execute(new Object[]{users});
    }

    @Test
    public void testStatsCounter() throws Exception {
        TypeToken<List<User>> pt = new TypeToken<List<User>>() {};
        TypeToken<int[]> rt = TypeToken.of(int[].class);
        String srcSql = "update user set name=:1.name where id=:1.id";
        Operator operator = getOperator(pt, rt, srcSql);

        StatsCounter sc = new StatsCounter();
        operator.setStatsCounter(sc);
        operator.setJdbcOperations(new JdbcOperationsAdapter() {
            @Override
            public int[] batchUpdate(DataSource ds, String sql, List<Object[]> batchArgs) {
                String descSql = "update user set name=? where id=?";
                assertThat(sql, equalTo(descSql));
                assertThat(batchArgs.size(), equalTo(2));
                assertThat(batchArgs.get(0)[0], equalTo((Object) "ash"));
                assertThat(batchArgs.get(0)[1], equalTo((Object) 100));
                assertThat(batchArgs.get(1)[0], equalTo((Object) "lucy"));
                assertThat(batchArgs.get(1)[1], equalTo((Object) 200));
                return new int[] {1};
            }
        });
        List<User> users = Arrays.asList(new User(100, "ash"), new User(200, "lucy"));
        operator.execute(new Object[]{users});
        assertThat(sc.snapshot().executeSuccessCount(), equalTo(1L));
        operator.execute(new Object[]{users});
        assertThat(sc.snapshot().executeSuccessCount(), equalTo(2L));

        operator.setJdbcOperations(new JdbcOperationsAdapter());
        try {
            operator.execute(new Object[]{users});
        } catch (UnsupportedOperationException e) {
        }
        assertThat(sc.snapshot().executeExceptionCount(), equalTo(1L));
        try {
            operator.execute(new Object[]{users});
        } catch (UnsupportedOperationException e) {
        }
        assertThat(sc.snapshot().executeExceptionCount(), equalTo(2L));
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

    private Operator getOperator2(TypeToken<?> pt, TypeToken<?> rt, String srcSql) throws Exception {
        List<Annotation> pAnnos = new ArrayList<Annotation>();
        pAnnos.add(new MockShardBy("id"));
        ParameterDescriptor p = new ParameterDescriptor(0, pt.getType(), pt.getRawType(), pAnnos, "1");
        List<ParameterDescriptor> pds = Arrays.asList(p);

        List<Annotation> methodAnnos = new ArrayList<Annotation>();
        methodAnnos.add(new MockDB("", "user", ModHundredTablePartition.class, MyDataSourceRouter.class));
        methodAnnos.add(new MockSQL(srcSql));
        MethodDescriptor md = new MethodDescriptor(rt.getType(), rt.getRawType(), methodAnnos, pds);


        Map<String, DataSourceFactory> map = new HashMap<String, DataSourceFactory>();
        map.put("l50", new SimpleDataSourceFactory(Config.getDataSource(0)));
        map.put("g50", new SimpleDataSourceFactory(Config.getDataSource(1)));
        DataSourceFactory dsf = new MultipleDataSourceFactory(map);
        OperatorFactory factory = new OperatorFactory(dsf, null, new InterceptorChain(), new InterceptorChain());
        Operator operator = factory.getOperator(md);
        return operator;
    }

    public static class MyDataSourceRouter implements DataSourceRouter {
        @Override
        public String getDataSourceName(Object shardParam) {
            Integer i = (Integer) shardParam;
            if (i < 50) {
                return "l50";
            } else {
                return "g50";
            }
        }
    }

}
