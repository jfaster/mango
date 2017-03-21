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

package org.jfaster.mango.interceptor;

import org.jfaster.mango.binding.BoundSql;
import org.jfaster.mango.binding.DefaultParameterContext;
import org.jfaster.mango.binding.InvocationContext;
import org.jfaster.mango.binding.InvocationContextFactory;
import org.jfaster.mango.descriptor.ParameterDescriptor;
import org.jfaster.mango.support.model4table.User;
import org.jfaster.mango.util.jdbc.SQLType;
import org.jfaster.mango.util.reflect.TypeToken;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class InvocationInterceptorChainTest {

  @Test
  public void testIntercept() throws Exception {
    final String sql = "select * from user where id=? and name=?";
    BoundSql boundSql = new BoundSql(sql);
    boundSql.addArg(1);
    boundSql.addArg("ash");
    final User user = new User();
    user.setId(100);
    user.setName("lucy");

    InterceptorChain ic = new InterceptorChain();
    ic.addInterceptor(new Interceptor() {
      @Override
      public void intercept(BoundSql boundSql, List<Parameter> parameters, SQLType sqlType, DataSource dataSource) {
        assertThat(boundSql.getSql(), equalTo(sql));
        assertThat(boundSql.getArgs(), equalTo(boundSql.getArgs()));
        assertThat(boundSql.getTypeHandlers(), equalTo(boundSql.getTypeHandlers()));
        assertThat((User) parameters.get(0).getValue(), equalTo(user));
        assertThat(sqlType, equalTo(SQLType.SELECT));
      }
    });
    List<Annotation> empty = Collections.emptyList();
    TypeToken<User> t = new TypeToken<User>() {
    };
    ParameterDescriptor p = ParameterDescriptor.create(0, t.getType(), empty, "1");
    List<ParameterDescriptor> pds = Arrays.asList(p);
    InvocationInterceptorChain iic = new InvocationInterceptorChain(ic, pds, SQLType.SELECT);

    InvocationContextFactory f = InvocationContextFactory.create(DefaultParameterContext.create(pds));
    InvocationContext ctx = f.newInvocationContext(new Object[]{user});
    iic.intercept(boundSql, ctx, null);
  }

}
