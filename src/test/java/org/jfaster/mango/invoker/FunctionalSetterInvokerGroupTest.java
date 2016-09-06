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

import org.jfaster.mango.mapper.FunctionalSetterInvokerGroup;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class FunctionalSetterInvokerGroupTest {

  @Test
  public void test() throws Exception {
    FunctionalSetterInvokerGroup gbi = FunctionalSetterInvokerGroup.create(A.class, "b.i");
    FunctionalSetterInvokerGroup gbj = FunctionalSetterInvokerGroup.create(A.class, "b.j");
    FunctionalSetterInvokerGroup gc = FunctionalSetterInvokerGroup.create(A.class, "b.c.k");
    A a = new A();
    gbi.invoke(a, 1);
    assertThat(a.getB().getI(), equalTo(1));
    assertThat(a.getB().getJ(), equalTo(0));
    assertThat(a.getB().getC(), nullValue());
    gbj.invoke(a, 2);
    assertThat(a.getB().getI(), equalTo(1));
    assertThat(a.getB().getJ(), equalTo(2));
    assertThat(a.getB().getC(), nullValue());
    gc.invoke(a, "ash");
    assertThat(a.getB().getI(), equalTo(1));
    assertThat(a.getB().getJ(), equalTo(2));
    assertThat(a.getB().getC().getK(), equalTo("ash"));
  }

  @Test
  public void testException() throws Exception {
    boolean error = false;
    try {
      FunctionalSetterInvokerGroup.create(A.class, "bb.i");
    } catch (UnreachablePropertyException e) {
      error = true;
    }
    assertThat(error, equalTo(true));

    error = false;
    try {
      FunctionalSetterInvokerGroup.create(A.class, "b.jj");
    } catch (UnreachablePropertyException e) {
      error = true;
    }
    assertThat(error, equalTo(true));
  }

  public static class A {
    private B b;

    public B getB() {
      return b;
    }

    public void setB(B b) {
      this.b = b;
    }

  }

  public static class B {
    private int i;
    private int j;
    private C c;

    public int getI() {
      return i;
    }

    public void setI(int i) {
      this.i = i;
    }

    public int getJ() {
      return j;
    }

    public void setJ(int j) {
      this.j = j;
    }

    public C getC() {
      return c;
    }

    public void setC(C c) {
      this.c = c;
    }
  }

  public static class C {
    private String k;

    public String getK() {
      return k;
    }

    public void setK(String k) {
      this.k = k;
    }
  }

}
