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

import org.jfaster.mango.annotation.Functional;
import org.jfaster.mango.invoker.FunctionalSetterInvoker;
import org.jfaster.mango.invoker.SetterInvoker;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class StringToLongArrayFunctionTest {

    @Test
    public void testApply() throws Exception {
        A a = new A();
        Method m = A.class.getDeclaredMethod("setX", long[].class);
        SetterInvoker invoker = FunctionalSetterInvoker.create("x", m);
        invoker.invoke(a, "1000000000000000000,2,3");
        assertThat(Arrays.toString(a.getX()), is(Arrays.toString(new long[] {1000000000000000000L, 2, 3})));
    }

    static class A {
        private long[] x;

        long[] getX() {
            return x;
        }

        @Functional(StringToLongArrayFunction.class)
        void setX(long[] x) {
            this.x = x;
        }
    }

}
