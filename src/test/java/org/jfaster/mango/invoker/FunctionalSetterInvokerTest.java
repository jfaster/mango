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

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import org.jfaster.mango.annotation.Setter;
import org.jfaster.mango.util.reflect.TypeToken;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class FunctionalSetterInvokerTest {

  public static class StringToIntegerFunction implements SetterFunction<String, Integer> {

    @Nullable
    @Override
    public Integer apply(@Nullable String input) {
      return Integer.valueOf(input);
    }

  }

  static class A {
    private int x;
    private int y;

    int getX() {
      return x;
    }

    void setX(int x) {
      this.x = x;
    }

    int getY() {
      return y;
    }

    @Setter(StringToIntegerFunction.class)
    void setY(int y) {
      this.y = y;
    }
  }

  @Test
  public void testStringToInt() throws Exception {
    Method method = A.class.getDeclaredMethod("setX", int.class);
    A a = new A();
    SetterInvoker invoker = FunctionalSetterInvoker.create("x", method);
    assertThat(invoker.getName(), is("x"));
    assertThat(int.class.equals(invoker.getParameterType()), is(true));
    assertThat(int.class.equals(invoker.getParameterRawType()), is(true));
    invoker.invoke(a, 100);
    assertThat(a.getX(), is(100));

    method = A.class.getDeclaredMethod("setY", int.class);
    a = new A();
    invoker = FunctionalSetterInvoker.create("y", method);
    assertThat(invoker.getName(), is("y"));
    assertThat(String.class.equals(invoker.getParameterType()), is(true));
    assertThat(String.class.equals(invoker.getParameterRawType()), is(true));
    invoker.invoke(a, "100");
    assertThat(a.getY(), is(100));
  }

  static class B {
    private Integer x;
    private Integer y;

    Integer getX() {
      return x;
    }

    void setX(Integer x) {
      this.x = x;
    }

    Integer getY() {
      return y;
    }

    @Setter(StringToIntegerFunction.class)
    void setY(Integer y) {
      this.y = y;
    }
  }

  @Test
  public void testStringToInteger() throws Exception {
    Method method = B.class.getDeclaredMethod("setX", Integer.class);
    B b = new B();
    SetterInvoker invoker = FunctionalSetterInvoker.create("x", method);
    assertThat(invoker.getName(), is("x"));
    assertThat(Integer.class.equals(invoker.getParameterType()), is(true));
    assertThat(Integer.class.equals(invoker.getParameterRawType()), is(true));
    invoker.invoke(b, 100);
    assertThat(b.getX(), is(100));

    method = B.class.getDeclaredMethod("setY", Integer.class);
    b = new B();
    invoker = FunctionalSetterInvoker.create("y", method);
    assertThat(invoker.getName(), is("y"));
    assertThat(String.class.equals(invoker.getParameterType()), is(true));
    assertThat(String.class.equals(invoker.getParameterRawType()), is(true));
    invoker.invoke(b, "100");
    assertThat(b.getY(), is(100));
  }

  public static class IntegerToStringFunction implements SetterFunction<Integer, String> {

    @Nullable
    @Override
    public String apply(@Nullable Integer input) {
      return "xx" + input;
    }

  }

  static class C {
    private String x;
    private String y;

    String getX() {
      return x;
    }

    void setX(String x) {
      this.x = x;
    }

    String getY() {
      return y;
    }

    @Setter(IntegerToStringFunction.class)
    void setY(String y) {
      this.y = y;
    }
  }

  @Test
  public void testIntegerToString() throws Exception {
    Method method = C.class.getDeclaredMethod("setX", String.class);
    C c = new C();
    SetterInvoker invoker = FunctionalSetterInvoker.create("x", method);
    assertThat(invoker.getName(), is("x"));
    assertThat(String.class.equals(invoker.getParameterType()), is(true));
    assertThat(String.class.equals(invoker.getParameterRawType()), is(true));
    invoker.invoke(c, "xx100");
    assertThat(c.getX(), is("xx100"));

    method = C.class.getDeclaredMethod("setY", String.class);
    c = new C();
    invoker = FunctionalSetterInvoker.create("y", method);
    assertThat(invoker.getName(), is("y"));
    assertThat(Integer.class.equals(invoker.getParameterType()), is(true));
    assertThat(Integer.class.equals(invoker.getParameterRawType()), is(true));
    invoker.invoke(c, 100);
    assertThat(c.getY(), is("xx100"));
  }

  public static class StringToIntegerListFunction implements SetterFunction<String, List<Integer>> {

    @Nullable
    @Override
    public List<Integer> apply(@Nullable String input) {
      List<Integer> r = Lists.newArrayList();
      for (String s : Splitter.on(",").split(input)) {
        r.add(Integer.valueOf(s));
      }
      return r;
    }

  }

  static class D {
    private List<Integer> x;
    private List<Integer> y;

    List<Integer> getX() {
      return x;
    }

    void setX(List<Integer> x) {
      this.x = x;
    }

    List<Integer> getY() {
      return y;
    }

    @Setter(StringToIntegerListFunction.class)
    void setY(List<Integer> y) {
      this.y = y;
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testStringToList() throws Exception {
    Method method = D.class.getDeclaredMethod("setX", List.class);
    D d = new D();
    SetterInvoker invoker = FunctionalSetterInvoker.create("x", method);
    assertThat(invoker.getName(), is("x"));
    Type type = new TypeToken<List<Integer>>() {
    }.getType();
    assertThat(type.equals(invoker.getParameterType()), is(true));
    assertThat(List.class.equals(invoker.getParameterRawType()), is(true));
    List<Integer> x = Lists.newArrayList(1, 2);
    invoker.invoke(d, x);
    assertThat(d.getX().equals(x), is(true));

    method = D.class.getDeclaredMethod("setY", List.class);
    d = new D();
    invoker = FunctionalSetterInvoker.create("y", method);
    assertThat(invoker.getName(), is("y"));
    assertThat(String.class.equals(invoker.getParameterType()), is(true));
    assertThat(String.class.equals(invoker.getParameterRawType()), is(true));
    invoker.invoke(d, "1,2");
    assertThat(d.getY().equals(x), is(true));
  }


  static class E {
    private String x;

    String getX() {
      return x;
    }

    @Setter(StringToIntegerFunction.class)
    void setX(String x) {
      this.x = x;
    }
  }


  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testException() throws Exception {
    thrown.expect(ClassCastException.class);
    thrown.expectMessage("function[class org.jfaster.mango.invoker.FunctionalSetterInvokerTest$StringToIntegerFunction] on method[void org.jfaster.mango.invoker.FunctionalSetterInvokerTest$E.setX(java.lang.String)] error, method's parameterType[class java.lang.String] must be assignable from function's outputType[class java.lang.Integer]");
    Method method = E.class.getDeclaredMethod("setX", String.class);
    FunctionalSetterInvoker.create("x", method);
  }

  static class F {
    private List<String> x;

    List<String> getX() {
      return x;
    }

    @Setter(StringToIntegerListFunction.class)
    void setX(List<String> x) {
      this.x = x;
    }
  }

  @Test
  public void testException2() throws Exception {
    thrown.expect(ClassCastException.class);
    thrown.expectMessage("function[class org.jfaster.mango.invoker.FunctionalSetterInvokerTest$StringToIntegerListFunction] on method[void org.jfaster.mango.invoker.FunctionalSetterInvokerTest$F.setX(java.util.List)] error, method's parameterType[java.util.List<java.lang.String>] must be assignable from function's outputType[java.util.List<java.lang.Integer>]");
    Method method = F.class.getDeclaredMethod("setX", List.class);
    FunctionalSetterInvoker.create("x", method);
  }

  public static class StringIntArrayFunction implements SetterFunction<String, int[]> {

    @Nullable
    @Override
    public int[] apply(@Nullable String input) {
      List<Integer> r = Lists.newArrayList();
      for (String s : Splitter.on(",").split(input)) {
        r.add(Integer.valueOf(s));
      }
      return Ints.toArray(r);
    }
  }

  static class G {
    private int[] x;
    private int[] y;

    int[] getX() {
      return x;
    }

    void setX(int[] x) {
      this.x = x;
    }

    int[] getY() {
      return y;
    }

    @Setter(StringIntArrayFunction.class)
    void setY(int[] y) {
      this.y = y;
    }
  }

  @Test
  public void testIntArrayToString() throws Exception {
    Method method = G.class.getDeclaredMethod("setX", int[].class);
    G g = new G();
    SetterInvoker invoker = FunctionalSetterInvoker.create("x", method);
    assertThat(invoker.getName(), is("x"));
    assertThat(int[].class.equals(invoker.getParameterType()), is(true));
    assertThat(int[].class.equals(invoker.getParameterRawType()), is(true));
    int[] x = new int[]{1, 2};
    invoker.invoke(g, x);
    assertThat(Arrays.toString(g.getX()).equals(Arrays.toString(x)), is(true));

    method = G.class.getDeclaredMethod("setY", int[].class);
    g = new G();
    invoker = FunctionalSetterInvoker.create("y", method);
    assertThat(invoker.getName(), is("y"));
    assertThat(String.class.equals(invoker.getParameterType()), is(true));
    assertThat(String.class.equals(invoker.getParameterRawType()), is(true));
    invoker.invoke(g, "1,2");
    assertThat(Arrays.toString(g.getY()).equals(Arrays.toString(x)), is(true));
  }

  static class H {
    private String x;

    @Setter(StringToIntegerFunction.class)
    String getX() {
      return x;
    }

    void setX(String x) {
      this.x = x;
    }
  }

}
