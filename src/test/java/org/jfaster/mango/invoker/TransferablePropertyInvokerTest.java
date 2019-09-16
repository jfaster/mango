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

package org.jfaster.mango.invoker;

import org.jfaster.mango.annotation.Transfer;
import org.jfaster.mango.invoker.transfer.IntegerListToStringTransfer;
import org.jfaster.mango.util.bean.BeanUtil;
import org.jfaster.mango.util.bean.PropertyMeta;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.lang.reflect.Type;
import java.util.List;

import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class TransferablePropertyInvokerTest {

  @Test
  public void invokeId() {
    PropertyMeta idMeta = BeanUtil.fetchPropertyMeta(ClassA.class, "id");
    TransferableInvoker idInvoker = TransferablePropertyInvoker.create(idMeta);

    assertThat(idInvoker.getName(), is("id"));
    assertThat(idInvoker.getColumnType(), is((Type) int.class));

    ClassA a = new ClassA();
    idInvoker.invokeSet(a, 100);
    assertThat(a.getId(), is(100));
    assertThat(idInvoker.invokeGet(a), is(100));
  }

  @Test
  public void invokeName() {
    PropertyMeta idMeta = BeanUtil.fetchPropertyMeta(ClassA.class, "name");
    TransferableInvoker nameInvoker = TransferablePropertyInvoker.create(idMeta);

    assertThat(nameInvoker.getName(), is("name"));
    assertThat(nameInvoker.getColumnType(), is((Type) String.class));

    ClassA a = new ClassA();
    nameInvoker.invokeSet(a, "ash");
    assertThat(a.getName(), is("ash"));
    assertThat(nameInvoker.invokeGet(a), is("ash"));

    nameInvoker.invokeSet(a, null);
    assertThat(a.getName(), nullValue());
    assertThat(nameInvoker.invokeGet(a), nullValue());
  }

  @Test
  public void invokeSubIds() {
    PropertyMeta idMeta = BeanUtil.fetchPropertyMeta(ClassA.class, "subIds");
    TransferableInvoker subIdsInvoker = TransferablePropertyInvoker.create(idMeta);

    assertThat(subIdsInvoker.getName(), is("subIds"));
    assertThat(subIdsInvoker.getColumnType(), is((Type) String.class));

    ClassA a = new ClassA();
    subIdsInvoker.invokeSet(a, "1,2,3");
    assertThat(a.getSubIds(), hasItems(1, 2, 3));
    assertThat(subIdsInvoker.invokeGet(a), is("1,2,3"));

    subIdsInvoker.invokeSet(a, null);
    assertThat(a.getSubIds(), nullValue());
    assertThat(subIdsInvoker.invokeGet(a), nullValue());
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void invokeId4Exception() {
    thrown.expect(NullPointerException.class);
    thrown.expectMessage("property id of class org.jfaster.mango.invoker.TransferablePropertyInvokerTest$ClassA is primitive, can not be assigned to null");
    PropertyMeta idMeta = BeanUtil.fetchPropertyMeta(ClassA.class, "id");
    TransferableInvoker idInvoker = TransferablePropertyInvoker.create(idMeta);
    ClassA a = new ClassA();
    idInvoker.invokeSet(a, null);
  }

  @Test
  public void invokeId4Exception2() {
    thrown.expect(ClassCastException.class);
    thrown.expectMessage("error transfer<List<Integer>, String> for property type int");
    PropertyMeta idMeta = BeanUtil.fetchPropertyMeta(ClassB.class, "id");
    TransferablePropertyInvoker.create(idMeta);
  }

  private static class ClassA {

    private int id;
    private String name;

    @Transfer(IntegerListToStringTransfer.class)
    private List<Integer> subIds;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }

    public String getName() {
      return name;
    }

    public void setName(String name) {
      this.name = name;
    }

    public List<Integer> getSubIds() {
      return subIds;
    }

    public void setSubIds(List<Integer> subIds) {
      this.subIds = subIds;
    }
  }

  private static class ClassB {

    @Transfer(IntegerListToStringTransfer.class)
    private int id;

    public int getId() {
      return id;
    }

    public void setId(int id) {
      this.id = id;
    }
  }

}