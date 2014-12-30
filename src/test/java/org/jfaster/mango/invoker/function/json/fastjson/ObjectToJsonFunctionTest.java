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

package org.jfaster.mango.invoker.function.json.fastjson;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Lists;
import org.jfaster.mango.annotation.Functional;
import org.jfaster.mango.invoker.FunctionalGetterInvoker;
import org.jfaster.mango.invoker.GetterInvoker;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class ObjectToJsonFunctionTest {

    @Test
    public void testApply() throws Exception {
        A a = new A();
        List<Integer> list = Lists.newArrayList(1, 2, 3);
        a.setList(list);
        Method m = A.class.getDeclaredMethod("getList");
        GetterInvoker invoker = FunctionalGetterInvoker.create("list", m);
        String r = (String) invoker.invoke(a);
        assertThat(r, is(JSON.toJSONString(list)));

        B b = new B(3, 5);
        a.setB(b);
        Method m2 = A.class.getDeclaredMethod("getB");
        GetterInvoker invoker2 = FunctionalGetterInvoker.create("b", m2);
        String r2 = (String) invoker2.invoke(a);
        assertThat(r2, is(JSON.toJSONString(b)));
    }

    static class A {
        private List<Integer> list;
        private B b;

        @Functional(ObjectToJsonFunction.class)
        List<Integer> getList() {
            return list;
        }

        void setList(List<Integer> list) {
            this.list = list;
        }

        @Functional(ObjectToJsonFunction.class)
        B getB() {
            return b;
        }

        void setB(B b) {
            this.b = b;
        }
    }

    public static class B {
        private int x;
        private int y;

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
    }

}
