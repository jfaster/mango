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

package org.jfaster.mango.invoker.function.json;

import com.google.common.collect.Lists;
import com.google.gson.Gson;
import org.jfaster.mango.annotation.Setter;
import org.jfaster.mango.invoker.FunctionalSetterInvoker;
import org.jfaster.mango.invoker.SetterInvoker;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class GsonToObjectFunctionTest {

  @Test
  public void testApply() throws Exception {
    A a = new A();
    List<Integer> list = Lists.newArrayList(1, 2, 3);
    String json = new Gson().toJson(list);
    Method m = A.class.getDeclaredMethod("setList", List.class);
    SetterInvoker invoker = FunctionalSetterInvoker.create("list", m);
    invoker.invoke(a, json);
    assertThat(a.getList().toString(), equalTo(list.toString()));

    B b = new B(3, 5);
    String json2 = new Gson().toJson(b);
    Method m2 = A.class.getDeclaredMethod("setB", B.class);
    SetterInvoker invoker2 = FunctionalSetterInvoker.create("b", m2);
    invoker2.invoke(a, json2);
    assertThat(a.getB(), equalTo(b));
  }

  @Test
  public void testApplyArray() throws Exception {
    G g = new G();

    int[] a = new int[]{2, 3, 4};
    String jsonA = new Gson().toJson(a);
    Method m = G.class.getDeclaredMethod("setA", int[].class);
    SetterInvoker invoker = FunctionalSetterInvoker.create("a", m);
    invoker.invoke(g, jsonA);
    assertThat(Arrays.toString(g.getA()), equalTo(Arrays.toString(a)));

    B[] b = new B[]{new B(4, 5), new B(7, 8)};
    String jsonB = new Gson().toJson(b);
    Method m2 = G.class.getDeclaredMethod("setB", B[].class);
    SetterInvoker invoker2 = FunctionalSetterInvoker.create("b", m2);
    invoker2.invoke(g, jsonB);
    assertThat(Arrays.toString(g.getB()), equalTo(Arrays.toString(b)));

    Integer[] c = new Integer[]{1, 4, 7};
    String jsonC = new Gson().toJson(c);
    Method m3 = G.class.getDeclaredMethod("setC", Integer[].class);
    SetterInvoker invoker3 = FunctionalSetterInvoker.create("c", m3);
    invoker3.invoke(g, jsonC);
    assertThat(Arrays.toString(g.getC()), equalTo(Arrays.toString(c)));
  }


  static class A {
    private List<Integer> list;
    private B b;

    List<Integer> getList() {
      return list;
    }

    @Setter(GsonToObjectFunction.class)
    void setList(List<Integer> list) {
      this.list = list;
    }

    B getB() {
      return b;
    }

    @Setter(GsonToObjectFunction.class)
    void setB(B b) {
      this.b = b;
    }
  }

  public static class B {
    private int x;
    private int y;

    public B() {
    }

    public B(int x, int y) {
      this.x = x;
      this.y = y;
    }

    public int getX() {
      return x;
    }

    public void setX(int x) {
      this.x = x;
    }

    public int getY() {
      return y;
    }

    public void setY(int y) {
      this.y = y;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj instanceof B) {
        B other = (B) obj;
        return this.x == other.x && this.y == other.y;
      }
      return false;
    }

    @Override
    public String toString() {
      return "x=" + x + ", y=" + y;
    }
  }

  static class G {
    private int[] a;
    private B[] b;
    private Integer[] c;

    int[] getA() {
      return a;
    }

    @Setter(GsonToObjectFunction.class)
    void setA(int[] a) {
      this.a = a;
    }

    B[] getB() {
      return b;
    }

    @Setter(GsonToObjectFunction.class)
    void setB(B[] b) {
      this.b = b;
    }

    Integer[] getC() {
      return c;
    }

    @Setter(GsonToObjectFunction.class)
    void setC(Integer[] c) {
      this.c = c;
    }
  }


}
