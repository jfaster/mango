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

import org.jfaster.mango.descriptor.ParameterDescriptor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author ash
 */
public class DefaultParameterContextTest {

  @Test
  public void testGetParameterNameByPosition() throws Exception {
    List<Annotation> empty = Collections.emptyList();
    ParameterDescriptor p0 = ParameterDescriptor.create(0, String.class, empty, "param1");
    ParameterDescriptor p1 = ParameterDescriptor.create(1, int.class, empty, "param2");
    List<ParameterDescriptor> pds = Arrays.asList(p0, p1);
    ParameterContext ctx = DefaultParameterContext.create(pds);
    assertThat(ctx.getParameterNameByPosition(0), equalTo("param1"));
    assertThat(ctx.getParameterNameByPosition(1), equalTo("param2"));
  }

  @Test
  public void testGetBindingParameterInvoker() throws Exception {
    List<Annotation> empty = Collections.emptyList();
    ParameterDescriptor p0 = ParameterDescriptor.create(0, String.class, empty, "1");
    ParameterDescriptor p1 = ParameterDescriptor.create(1, User.class, empty, "2");
    List<ParameterDescriptor> pds = Arrays.asList(p0, p1);

    ParameterContext ctx = DefaultParameterContext.create(pds);
    checkBindingParameterInvoker(ctx, "1", "", String.class);
    checkBindingParameterInvoker(ctx, "2", "userBag.item.itemId", int.class);
    checkBindingParameterInvoker(ctx, "2", "userBag.item.objItemId", Integer.class);
    checkBindingParameterInvoker(ctx, "2", "userId", String.class);
  }

  private void checkBindingParameterInvoker(ParameterContext ctx, String parameterName, String propertyPath, Type type) {
    assertThat(ctx.getBindingParameterInvoker(BindingParameter.create(parameterName, propertyPath, null)).getTargetType(), equalTo(type));
  }

  @Test
  public void testGetParameterDescriptors() throws Exception {
    List<Annotation> empty = Collections.emptyList();
    ParameterDescriptor p0 = ParameterDescriptor.create(0, String.class, empty, "1");
    ParameterDescriptor p1 = ParameterDescriptor.create(1, User.class, empty, "2");
    List<ParameterDescriptor> pds = Arrays.asList(p0, p1);

    ParameterContext ctx = DefaultParameterContext.create(pds);
    assertThat(ctx.getParameterDescriptors(), equalTo(pds));
  }


  @Test
  public void testTryExpandBindingParameter() throws Exception {
    List<Annotation> empty = Collections.emptyList();
    ParameterDescriptor p0 = ParameterDescriptor.create(0, String.class, empty, "1");
    ParameterDescriptor p1 = ParameterDescriptor.create(1, User.class, empty, "2");
    List<ParameterDescriptor> pds = Arrays.asList(p0, p1);

    ParameterContext ctx = DefaultParameterContext.create(pds);

    BindingParameter bp = BindingParameter.create("userBag", "item.itemId", null);
    BindingParameter nbp = ctx.tryExpandBindingParameter(bp);
    assertThat(nbp, equalTo(BindingParameter.create("2", "userBag.item.itemId", null)));

    bp = BindingParameter.create("userId", "", null);
    nbp = ctx.tryExpandBindingParameter(bp);
    assertThat(nbp, equalTo(BindingParameter.create("2", "userId", null)));

    bp = BindingParameter.create("userIds", "", null);
    nbp = ctx.tryExpandBindingParameter(bp);
    assertThat(nbp, nullValue());
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testIllegalStateException() throws Exception {
    thrown.expect(IllegalStateException.class);
    List<Annotation> empty = Collections.emptyList();
    ParameterDescriptor p0 = ParameterDescriptor.create(0, String.class, empty, "param1");
    List<ParameterDescriptor> pds = Arrays.asList(p0);
    ParameterContext ctx = DefaultParameterContext.create(pds);
    ctx.getParameterNameByPosition(1);
  }

  @Test
  public void testBindingException() throws Exception {
    thrown.expect(BindingException.class);
    thrown.expectMessage("Parameter ':user' not found, available root parameters are [:param1, :param2]");
    List<Annotation> empty = Collections.emptyList();
    ParameterDescriptor p0 = ParameterDescriptor.create(0, String.class, empty, "param1");
    ParameterDescriptor p1 = ParameterDescriptor.create(1, String.class, empty, "param2");
    List<ParameterDescriptor> pds = Arrays.asList(p0, p1);
    ParameterContext ctx = DefaultParameterContext.create(pds);
    ctx.getBindingParameterInvoker(BindingParameter.create("user", "id", null));
  }

  @Test
  public void testBindingException2() throws Exception {
    thrown.expect(BindingException.class);
    thrown.expectMessage("Root parameters [:1, :2] has the same property 'userId', so can't auto expand");
    List<Annotation> empty = Collections.emptyList();
    ParameterDescriptor p0 = ParameterDescriptor.create(0, User.class, empty, "1");
    ParameterDescriptor p1 = ParameterDescriptor.create(1, User.class, empty, "2");
    List<ParameterDescriptor> pds = Arrays.asList(p0, p1);
    ParameterContext ctx = DefaultParameterContext.create(pds);
    ctx.tryExpandBindingParameter(BindingParameter.create("userId", "", null));
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
