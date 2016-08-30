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

import com.google.common.collect.Lists;
import org.jfaster.mango.annotation.Setter;
import org.jfaster.mango.invoker.FunctionalSetterInvoker;
import org.jfaster.mango.invoker.SetterInvoker;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class StringToIntegerListFunctionTest {

  @Test
  public void testApply() throws Exception {
    A a = new A();
    Method m = A.class.getDeclaredMethod("setX", List.class);
    SetterInvoker invoker = FunctionalSetterInvoker.create("x", m);

    invoker.invoke(a, "1,2,3");
    List<Integer> list = Lists.newArrayList(1, 2, 3);
    assertThat(a.getX().toString(), is(list.toString()));

    invoker.invoke(a, null);
    assertThat(a.getX(), nullValue());

    invoker.invoke(a, "");
    assertThat(a.getX().toString(), is(new ArrayList<Integer>().toString()));
  }

  static class A {
    private List<Integer> x;

    List<Integer> getX() {
      return x;
    }

    @Setter(StringToIntegerListFunction.class)
    void setX(List<Integer> x) {
      this.x = x;
    }
  }

}
