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

import java.lang.reflect.Type;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author ash
 */
public class FunctionalBindingParameterInvokerTest {

  @Test
  public void testAll() throws Exception {
    BindingParameter bp = BindingParameter.create("user", "userBag.item.itemId", null);
    BindingParameter bp2 = BindingParameter.create("user", "userBag.item.objItemId", null);
    BindingParameter bp3 = BindingParameter.create("user", "userId", null);
    BindingParameterInvoker invoker = FunctionalBindingParameterInvoker.create(User.class, bp);
    BindingParameterInvoker invoker2 = FunctionalBindingParameterInvoker.create(User.class, bp2);
    BindingParameterInvoker invoker3 = FunctionalBindingParameterInvoker.create(User.class, bp3);

    assertThat(invoker.getBindingParameter(), equalTo(bp));
    assertThat(invoker2.getBindingParameter(), equalTo(bp2));
    assertThat(invoker3.getBindingParameter(), equalTo(bp3));

    assertThat(invoker.getTargetType(), equalTo((Type) int.class));
    assertThat(invoker2.getTargetType(), equalTo((Type) Integer.class));
    assertThat(invoker3.getTargetType(), equalTo((Type) String.class));


    Item item = new Item();
    item.setItemId(9527);
    UserBag userBag = new UserBag();
    userBag.setItem(item);
    User user = new User();
    user.setUserId("ash");
    user.setUserBag(userBag);

    assertThat(invoker.invoke(user), equalTo((Object) 9527));
    assertThat(invoker2.invoke(user), nullValue());
    assertThat(invoker3.invoke(user), equalTo((Object) "ash"));
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testBindingException() throws Exception {
    thrown.expect(BindingException.class);
    thrown.expectMessage("Parameter ':user' is null");
    BindingParameterInvoker invoker = FunctionalBindingParameterInvoker.create(
        User.class, BindingParameter.create("user", "userBag.item.itemId", null));
    invoker.invoke(null);
  }

  @Test
  public void testBindingException2() throws Exception {
    thrown.expect(BindingException.class);
    thrown.expectMessage("Parameter ':user.userBag' is null");
    BindingParameterInvoker invoker = FunctionalBindingParameterInvoker.create(
        User.class, BindingParameter.create("user", "userBag.item.itemId", null));
    User user = new User();
    user.setUserId("ash");
    invoker.invoke(user);
  }

  @Test
  public void testBindingException3() throws Exception {
    thrown.expect(BindingException.class);
    thrown.expectMessage("Parameter ':user.userBag.item' is null");
    BindingParameterInvoker invoker = FunctionalBindingParameterInvoker.create(
        User.class, BindingParameter.create("user", "userBag.item.itemId", null));
    UserBag userBag = new UserBag();
    User user = new User();
    user.setUserId("ash");
    user.setUserBag(userBag);
    invoker.invoke(user);
  }

  @Test
  public void testBindingException4() throws Exception {
    thrown.expect(BindingException.class);
    thrown.expectMessage("Parameter ':user.userBag.ite' can't be readable; " +
        "caused by: There is no getter/setter for property named 'ite' in 'class org.jfaster.mango.binding.FunctionalBindingParameterInvokerTest$UserBag'");
    FunctionalBindingParameterInvoker.create(User.class, BindingParameter.create("user", "userBag.ite", null));
  }

  public static class User {
    private UserBag userBag;
    private String userId;

    public UserBag getUserBag() {
      return userBag;
    }

    public void setUserBag(UserBag userBag) {
      this.userBag = userBag;
    }

    public String getUserId() {
      return userId;
    }

    public void setUserId(String userId) {
      this.userId = userId;
    }
  }

  public static class UserBag {
    private Item item;

    public Item getItem() {
      return item;
    }

    public void setItem(Item item) {
      this.item = item;
    }
  }

  public static class Item {
    private int itemId;
    private Integer objItemId;

    public int getItemId() {
      return itemId;
    }

    public void setItemId(int itemId) {
      this.itemId = itemId;
    }

    public Integer getObjItemId() {
      return objItemId;
    }

    public void setObjItemId(Integer objItemId) {
      this.objItemId = objItemId;
    }
  }

}
