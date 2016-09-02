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

package org.jfaster.mango.binding;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author ash
 */
public class DefaultInvocationContextTest {

  @Test
  public void testGetNullableBindingValue() throws Exception {
    DefaultInvocationContext ctx = DefaultInvocationContext.create();
    UserBag userBag = new UserBag();
    userBag.setName("ash");
    User user = new User();
    user.setUserBag(userBag);
    user.setId(100);

    ctx.addParameter("userId", 9527);
    ctx.addParameter("user", user);

    BindingParameterInvoker userIdInvoker =
        FunctionalBindingParameterInvoker.create(User.class, BindingParameter.create("userId", "", null));
    assertThat(ctx.getNullableBindingValue(userIdInvoker), equalTo((Object) 9527));
    assertThat(ctx.getNullableBindingValue(userIdInvoker), equalTo((Object) 9527));

    BindingParameterInvoker userDotIdInvoker =
        FunctionalBindingParameterInvoker.create(User.class, BindingParameter.create("user", "id", null));
    assertThat(ctx.getNullableBindingValue(userDotIdInvoker), equalTo((Object) 100));
    assertThat(ctx.getNullableBindingValue(userDotIdInvoker), equalTo((Object) 100));

    BindingParameterInvoker userDotObjIdInvoker =
        FunctionalBindingParameterInvoker.create(User.class, BindingParameter.create("user", "objId", null));
    assertThat(ctx.getNullableBindingValue(userDotObjIdInvoker), nullValue());
    assertThat(ctx.getNullableBindingValue(userDotObjIdInvoker), nullValue());

    BindingParameterInvoker userDotUserBagDotNameInvoker =
        FunctionalBindingParameterInvoker.create(User.class, BindingParameter.create("user", "userBag.name", null));
    assertThat(ctx.getNullableBindingValue(userDotUserBagDotNameInvoker), equalTo((Object) "ash"));
    assertThat(ctx.getNullableBindingValue(userDotUserBagDotNameInvoker), equalTo((Object) "ash"));
  }

  @Test
  public void testGetBindingValue() throws Exception {
    DefaultInvocationContext ctx = DefaultInvocationContext.create();
    ctx.addParameter("userId", 9527);
    BindingParameterInvoker userIdInvoker =
        FunctionalBindingParameterInvoker.create(User.class, BindingParameter.create("userId", "", null));
    assertThat(ctx.getBindingValue(userIdInvoker), equalTo((Object) 9527));
  }

  @Test
  public void testOtherMethod() throws Exception {
    DefaultInvocationContext ctx = DefaultInvocationContext.create();
    String table = "t_user";
    ctx.setGlobalTable(table);
    assertThat(ctx.getGlobalTable(), equalTo(table));
    ctx.writeToSqlBuffer("select ");
    ctx.writeToSqlBuffer("* ");
    ctx.writeToSqlBuffer("from ");
    ctx.writeToSqlBuffer("t_user");
    List<Object> objs = new ArrayList<Object>();
    objs.add(1);
    objs.add("ash");
    int t = 0;
    for (Object obj : objs) {
      ctx.addParameter("id" + t++, obj);
      // TODO
      ctx.appendToArgs(obj, null);
    }
    BoundSql boundSql = ctx.getBoundSql();
    assertThat(boundSql.getSql(), equalTo("select * from t_user"));
    assertThat(boundSql.getArgs(), equalTo(objs));
    assertThat(ctx.getParameterValues(), equalTo(objs));
  }

  @Test
  public void testSetBindingValue() throws Exception {
    DefaultInvocationContext ctx = DefaultInvocationContext.create();
    ctx.addParameter("userId", 9527);
    BindingParameterInvoker userIdInvoker =
        FunctionalBindingParameterInvoker.create(User.class, BindingParameter.create("userId", "", null));
    assertThat(ctx.getBindingValue(userIdInvoker), equalTo((Object) 9527));
    ctx.setBindingValue(userIdInvoker, 666);
    assertThat(ctx.getBindingValue(userIdInvoker), equalTo((Object) 666));
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testBindingException() throws Exception {
    thrown.expect(BindingException.class);
    thrown.expectMessage("Parameter ':userId' need a non-null value");
    DefaultInvocationContext ctx = DefaultInvocationContext.create();
    ctx.addParameter("userId", null);
    BindingParameterInvoker userIdInvoker =
        FunctionalBindingParameterInvoker.create(User.class, BindingParameter.create("userId", "", null));
    ctx.getBindingValue(userIdInvoker);
  }

  @Test
  public void testBindingException2() throws Exception {
    thrown.expect(BindingException.class);
    thrown.expectMessage("Parameter ':userId2' not found, available root parameters are [:userId, :userName]");
    DefaultInvocationContext ctx = DefaultInvocationContext.create();
    ctx.addParameter("userId", null);
    ctx.addParameter("userName", "ash");
    BindingParameterInvoker userIdInvoker =
        FunctionalBindingParameterInvoker.create(User.class, BindingParameter.create("userId2", "", null));
    ctx.getNullableBindingValue(userIdInvoker);
  }

  public static class User {

    private int id;

    private Integer objId;

    private UserBag userBag;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public Integer getObjId() {
      return objId;
    }

    public void setObjId(Integer objId) {
      this.objId = objId;
    }

    public UserBag getUserBag() {
      return userBag;
    }

    public void setUserBag(UserBag userBag) {
      this.userBag = userBag;
    }
  }

  public static class UserBag {

    private String name;

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }
  }

}
