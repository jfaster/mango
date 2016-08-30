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
public class StringToEnumFunctionTest {

  @Test
  public void testApply() throws Exception {
    A a = new A();
    Method m = A.class.getDeclaredMethod("setE", E.class);
    SetterInvoker invoker = FunctionalSetterInvoker.create("e", m);
    invoker.invoke(a, "Y");
    assertThat(a.getE(), is(E.Y));
  }

  static class A {
    private E e;

    E getE() {
      return e;
    }

    @Setter(StringToEnumFunction.class)
    void setE(E e) {
      this.e = e;
    }
  }

  enum E {
    X, Y, Z;
  }

}
