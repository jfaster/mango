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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.google.common.primitives.Ints;
import org.jfaster.mango.annotation.Getter;
import org.jfaster.mango.invoker.function.LongListToStringFunction;
import org.jfaster.mango.util.reflect.TypeToken;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.annotation.Nullable;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class FunctionalGetterInvokerTest {

  public static class IntegerToStringFunction implements GetterFunction<Integer, String> {

    @Nullable
    @Override
    public String apply(@Nullable Integer input) {
      return "string" + input;
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

    @Getter(IntegerToStringFunction.class)
    int getY() {
      return y;
    }

    void setY(int y) {
      this.y = y;
    }
  }

  @Test
  public void testIntToString() throws Exception {
    Method method = A.class.getDeclaredMethod("getX");
    A a = new A();
    a.setX(100);
    GetterInvoker invoker = FunctionalGetterInvoker.create("x", method);
    assertThat(invoker.getName(), is("x"));
    assertThat(int.class.equals(invoker.getReturnType()), is(true));
    assertThat(int.class.equals(invoker.getReturnRawType()), is(true));
    assertThat((Integer) invoker.invoke(a), is(100));

    method = A.class.getDeclaredMethod("getY");
    a = new A();
    a.setY(100);
    invoker = FunctionalGetterInvoker.create("y", method);
    assertThat(invoker.getName(), is("y"));
    assertThat(String.class.equals(invoker.getReturnType()), is(true));
    assertThat(String.class.equals(invoker.getReturnRawType()), is(true));
    assertThat((String) invoker.invoke(a), is("string100"));
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

    @Getter(IntegerToStringFunction.class)
    Integer getY() {
      return y;
    }

    void setY(Integer y) {
      this.y = y;
    }
  }

  @Test
  public void testIntegerToString() throws Exception {
    Method method = B.class.getDeclaredMethod("getX");
    B b = new B();
    b.setX(100);
    GetterInvoker invoker = FunctionalGetterInvoker.create("x", method);
    assertThat(invoker.getName(), is("x"));
    assertThat(Integer.class.equals(invoker.getReturnType()), is(true));
    assertThat(Integer.class.equals(invoker.getReturnRawType()), is(true));
    assertThat((Integer) invoker.invoke(b), is(100));

    method = B.class.getDeclaredMethod("getY");
    b = new B();
    b.setY(100);
    invoker = FunctionalGetterInvoker.create("y", method);
    assertThat(invoker.getName(), is("y"));
    assertThat(String.class.equals(invoker.getReturnType()), is(true));
    assertThat(String.class.equals(invoker.getReturnRawType()), is(true));
    assertThat((String) invoker.invoke(b), is("string100"));
  }

  public static class StringToIntegerFunction implements GetterFunction<String, Integer> {

    @Nullable
    @Override
    public Integer apply(@Nullable String input) {
      return 100 * Integer.valueOf(input);
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

    @Getter(StringToIntegerFunction.class)
    String getY() {
      return y;
    }

    void setY(String y) {
      this.y = y;
    }
  }

  @Test
  public void testStringToInteger() throws Exception {
    Method method = C.class.getDeclaredMethod("getX");
    C c = new C();
    c.setX("9527");
    GetterInvoker invoker = FunctionalGetterInvoker.create("x", method);
    assertThat(invoker.getName(), is("x"));
    assertThat(String.class.equals(invoker.getReturnType()), is(true));
    assertThat(String.class.equals(invoker.getReturnRawType()), is(true));
    assertThat((String) invoker.invoke(c), is("9527"));

    method = C.class.getDeclaredMethod("getY");
    c = new C();
    c.setY("9527");
    invoker = FunctionalGetterInvoker.create("y", method);
    assertThat(invoker.getName(), is("y"));
    assertThat(Integer.class.equals(invoker.getReturnType()), is(true));
    assertThat(Integer.class.equals(invoker.getReturnRawType()), is(true));
    assertThat((Integer) invoker.invoke(c), is(952700));
  }

  public static class IntegerListToStringFunction implements GetterFunction<List<Integer>, String> {

    @Nullable
    @Override
    public String apply(@Nullable List<Integer> input) {
      return Joiner.on(",").join(input);
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

    @Getter(IntegerListToStringFunction.class)
    List<Integer> getY() {
      return y;
    }

    void setY(List<Integer> y) {
      this.y = y;
    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testListToString() throws Exception {
    Method method = D.class.getDeclaredMethod("getX");
    D d = new D();
    List<Integer> x = Lists.newArrayList(1, 2);
    d.setX(x);
    GetterInvoker invoker = FunctionalGetterInvoker.create("x", method);
    assertThat(invoker.getName(), is("x"));
    Type type = new TypeToken<List<Integer>>() {
    }.getType();
    assertThat(type.equals(invoker.getReturnType()), is(true));
    assertThat(List.class.equals(invoker.getReturnRawType()), is(true));
    assertThat(invoker.invoke(d).equals(x), is(true));

    method = D.class.getDeclaredMethod("getY");
    d = new D();
    List<Integer> y = Lists.newArrayList(1, 2);
    d.setY(y);
    invoker = FunctionalGetterInvoker.create("y", method);
    assertThat(invoker.getName(), is("y"));
    assertThat(String.class.equals(invoker.getReturnType()), is(true));
    assertThat(String.class.equals(invoker.getReturnRawType()), is(true));
    assertThat((String) invoker.invoke(d), is("1,2"));
  }


  static class E {
    private String x;

    @Getter(IntegerToStringFunction.class)
    String getX() {
      return x;
    }

    void setX(String x) {
      this.x = x;
    }
  }


  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testException() throws Exception {
    thrown.expect(ClassCastException.class);
    thrown.expectMessage("function[class org.jfaster.mango.invoker.FunctionalGetterInvokerTest$IntegerToStringFunction] on method[java.lang.String org.jfaster.mango.invoker.FunctionalGetterInvokerTest$E.getX()] error, function's inputType[class java.lang.Integer] must be assignable from method's returnType[class java.lang.String]");
    Method method = E.class.getDeclaredMethod("getX");
    E e = new E();
    e.setX("9527");
    FunctionalGetterInvoker.create("x", method);
  }

  static class F {
    private List<String> x;

    @Getter(IntegerListToStringFunction.class)
    List<String> getX() {
      return x;
    }

    void setX(List<String> x) {
      this.x = x;
    }
  }

  @Test
  public void testException2() throws Exception {
    thrown.expect(ClassCastException.class);
    thrown.expectMessage("function[class org.jfaster.mango.invoker.FunctionalGetterInvokerTest$IntegerListToStringFunction] on method[java.util.List org.jfaster.mango.invoker.FunctionalGetterInvokerTest$F.getX()] error, function's inputType[java.util.List<java.lang.Integer>] must be assignable from method's returnType[java.util.List<java.lang.String>]");
    Method method = F.class.getDeclaredMethod("getX");
    F e = new F();
    ArrayList<String> x = Lists.newArrayList("xxx");
    e.setX(x);
    FunctionalGetterInvoker.create("x", method);
  }

  public static class IntArrayToStringFunction implements GetterFunction<int[], String> {

    @Nullable
    @Override
    public String apply(@Nullable int[] input) {
      return Ints.join(",", input);
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

    @Getter(IntArrayToStringFunction.class)
    int[] getY() {
      return y;
    }

    void setY(int[] y) {
      this.y = y;
    }
  }

  @Test
  public void testIntArrayToString() throws Exception {
    Method method = G.class.getDeclaredMethod("getX");
    G g = new G();
    int[] x = new int[]{1, 2, 3};
    g.setX(x);
    GetterInvoker invoker = FunctionalGetterInvoker.create("x", method);
    assertThat(invoker.getName(), is("x"));
    assertThat(int[].class.equals(invoker.getReturnType()), is(true));
    assertThat(int[].class.equals(invoker.getReturnRawType()), is(true));
    assertThat(Arrays.toString((int[]) invoker.invoke(g)).equals(Arrays.toString(x)), is(true));

    method = G.class.getDeclaredMethod("getY");
    g = new G();
    int[] y = new int[]{1, 2, 3};
    g.setY(y);
    invoker = FunctionalGetterInvoker.create("y", method);
    assertThat(invoker.getName(), is("y"));
    assertThat(String.class.equals(invoker.getReturnType()), is(true));
    assertThat(String.class.equals(invoker.getReturnRawType()), is(true));
    assertThat((String) invoker.invoke(g), is("1,2,3"));
  }

  static class H {
    private String x;

    String getX() {
      return x;
    }

    @Getter(LongListToStringFunction.class)
    void setX(String x) {
      this.x = x;
    }
  }

}
