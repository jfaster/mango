/*
 * Copyright 2014 mango.concurrent.cc
 *
 * The Netty Project licenses this file to you under the Apache License,
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

package cc.concurrent.mango.exception;

import cc.concurrent.mango.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试{@link NotReadablePropertyException}
 *
 * @author ash
 */
public class NotReadablePropertyExceptionTest {

    private final static Mango mango = new Mango(Config.getDataSource());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() {
        thrown.expect(NotReadablePropertyException.class);
        thrown.expectMessage("property ':1.b.d' is not readable");
        Dao dao = mango.create(Dao.class);
        dao.add(new A());
    }

    @Test
    public void test2() {
        thrown.expect(NotReadablePropertyException.class);
        thrown.expectMessage("property ':1.c' is not readable");
        Dao dao = mango.create(Dao.class);
        dao.add2(new A());
    }

    @Test
    public void test3() {
        thrown.expect(NotReadablePropertyException.class);
        thrown.expectMessage("if use cache and sql has one in clause, property c of " +
                "class cc.concurrent.mango.exception.NotReadablePropertyExceptionTest$A " +
                "expected readable but not");
        Dao2 dao = mango.create(Dao2.class);
        dao.gets(new ArrayList<Integer>());
    }

    @DB
    static interface Dao {
        @SQL("insert into ... :1.b.d ...")
        public int add(A a);

        @SQL("insert into ... :1.c.d ...")
        public int add2(A a);
    }

    @DB
    @Cache(prefix = "dao2_", expire = Day.class)
    static interface Dao2 {
        @SQL("select ... where c in (:1)")
        public List<A> gets(@CacheBy List<Integer> ids);
    }

    static class A {
        B b;

        public B getB() {
            return b;
        }
    }

    static class B {
        C c;

        public C getC() {
            return c;
        }
    }

    static class C {

    }

}
