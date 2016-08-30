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

package org.jfaster.mango.invoker.function.enums;

import org.jfaster.mango.annotation.Setter;
import org.jfaster.mango.invoker.FunctionalSetterInvoker;
import org.jfaster.mango.invoker.SetterInvoker;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class IntegerToEnumFunctionTest {

  @Test
  public void testApply() throws Exception {
    A a = new A();
    Method m = A.class.getDeclaredMethod("setE", E.class);
    SetterInvoker invoker = FunctionalSetterInvoker.create("e", m);
    invoker.invoke(a, 2);
    assertThat(a.getE(), is(E.Z));


    Method m2 = A.class.getDeclaredMethod("setE2", E2.class);
    SetterInvoker invoker2 = FunctionalSetterInvoker.create("e2", m2);
    invoker2.invoke(a, 2);
    assertThat(a.getE2(), is(E2.C));
  }

  static class A {
    private E e;

    private E2 e2;

    E getE() {
      return e;
    }

    @Setter(IntegerToEnumFunction.class)
    void setE(E e) {
      this.e = e;
    }

    E2 getE2() {
      return e2;
    }

    @Setter(IntegerToEnumFunction.class)
    void setE2(E2 e2) {
      this.e2 = e2;
    }
  }

  enum E {
    X, Y, Z;
  }

  enum E2 {
    A, B, C;
  }

}
