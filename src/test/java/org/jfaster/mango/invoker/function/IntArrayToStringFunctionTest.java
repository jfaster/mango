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

package org.jfaster.mango.invoker.function;

import org.jfaster.mango.annotation.Getter;
import org.jfaster.mango.invoker.FunctionalGetterInvoker;
import org.jfaster.mango.invoker.GetterInvoker;
import org.junit.Test;

import java.lang.reflect.Method;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class IntArrayToStringFunctionTest {

  @Test
  public void testApply() throws Exception {
    A a = new A();
    Method m = A.class.getDeclaredMethod("getX");
    GetterInvoker invoker = FunctionalGetterInvoker.create("x", m);

    a.setX(new int[]{1, 2, 3});
    assertThat((String) invoker.invoke(a), is("1,2,3"));

    a.setX(null);
    assertThat(invoker.invoke(a), nullValue());

    a.setX(new int[]{});
    assertThat((String) invoker.invoke(a), is(""));

  }

  static class A {
    private int[] x;

    @Getter(IntArrayToStringFunction.class)
    int[] getX() {
      return x;
    }

    void setX(int[] x) {
      this.x = x;
    }
  }

}
