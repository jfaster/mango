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

import org.jfaster.mango.datasource.SimpleDataSourceFactory;
import org.jfaster.mango.descriptor.MethodDescriptor;
import org.jfaster.mango.descriptor.ParameterDescriptor;
import org.jfaster.mango.descriptor.ReturnDescriptor;
import org.jfaster.mango.interceptor.InterceptorChain;
import org.jfaster.mango.operator.ConfigHolder;
import org.jfaster.mango.operator.Operator;
import org.jfaster.mango.operator.OperatorFactory;
import org.jfaster.mango.stat.StatsCounter;
import org.jfaster.mango.support.*;
import org.jfaster.mango.util.reflect.TypeToken;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author ash
 */
public class CacheableBatchUpdateOperatorTest {

  // TODO 测试
//  @Test
//  public void testBatchUpdate() throws Exception {
//    TypeToken<List<User>> pt = new TypeToken<List<User>>() {
//    };
//    TypeToken<int[]> rt = TypeToken.of(int[].class);
//    String srcSql = "update user set name=:1.name where id=:1.id";
//    StatsCounter sc = new StatsCounter();
//    Operator operator = getOperator(pt, rt, srcSql, new CacheHandlerAdapter() {
//      @Override
//      public void batchDelete(Set<String> keys, Class<?> daoClass) {
//        Set<String> set = new HashSet<String>();
//        set.add("user_100");
//        set.add("user_200");
//        assertThat(keys, equalTo(set));
//      }
//    }, new MockCacheBy("id"), sc);
//
//    final int[] expectedInts = new int[]{1, 2};
//    operator.setJdbcOperations(new JdbcOperationsAdapter() {
//      @Override
//      public int[] batchUpdate(DataSource ds, String sql, List<Object[]> batchArgs) {
//        String descSql = "update user set name=? where id=?";
//        assertThat(sql, equalTo(descSql));
//        assertThat(batchArgs.size(), equalTo(2));
//        assertThat(batchArgs.get(0)[0], equalTo((Object) "ash"));
//        assertThat(batchArgs.get(0)[1], equalTo((Object) 100));
//        assertThat(batchArgs.get(1)[0], equalTo((Object) "lucy"));
//        assertThat(batchArgs.get(1)[1], equalTo((Object) 200));
//        return expectedInts;
//      }
//    });
//    List<User> users = Arrays.asList(new User(100, "ash"), new User(200, "lucy"));
//    int[] actualInts = (int[]) operator.execute(new Object[]{users});
//    assertThat(Arrays.toString(actualInts), equalTo(Arrays.toString(expectedInts)));
//    assertThat(sc.snapshot().getCacheBatchDeleteSuccessCount(), equalTo(1L));
//  }

  private Operator getOperator(TypeToken<?> pt, TypeToken<?> rt, String srcSql,
                               CacheHandler ch, MockCacheBy cacheBy, StatsCounter sc) throws Exception {
    List<Annotation> pAnnos = new ArrayList<Annotation>();
    pAnnos.add(cacheBy);
    ParameterDescriptor p = ParameterDescriptor.create(0, pt.getType(), pAnnos, "1");
    List<ParameterDescriptor> pds = Arrays.asList(p);

    List<Annotation> methodAnnos = new ArrayList<Annotation>();
    methodAnnos.add(new MockDB());
    methodAnnos.add(new MockCache("user", Day.class));
    methodAnnos.add(new MockSQL(srcSql));
    ReturnDescriptor rd = ReturnDescriptor.create(rt.getType(), methodAnnos);
    MethodDescriptor md = MethodDescriptor.create(null, rd, pds);

    OperatorFactory factory = new OperatorFactory(
        new SimpleDataSourceFactory(DataSourceConfig.getDataSource()), ch,
        new InterceptorChain(), null, new ConfigHolder());

    Operator operator = factory.getOperator(md, sc);
    return operator;
  }

}
