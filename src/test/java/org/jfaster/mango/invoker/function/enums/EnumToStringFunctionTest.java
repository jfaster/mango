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

import org.jfaster.mango.annotation.Getter;
import org.jfaster.mango.invoker.FunctionalGetterInvoker;
import org.jfaster.mango.invoker.GetterInvoker;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class EnumToStringFunctionTest {

  @Test
  public void testApply() throws Exception {
    A a = new A();
    a.setE(E.Y);
    Method m = A.class.getDeclaredMethod("getE");
    GetterInvoker invoker = FunctionalGetterInvoker.create("e", m);
    String r = (String) invoker.invoke(a);
    assertThat(r, is("Y"));
  }

  static class A {
    private E e;

    @Getter(EnumToStringFunction.class)
    E getE() {
      return e;
    }

    void setE(E e) {
      this.e = e;
    }
  }

  enum E {
    X, Y, Z;
  }

}
