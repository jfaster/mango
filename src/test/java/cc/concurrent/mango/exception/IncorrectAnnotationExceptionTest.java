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

/**
 * 测试{@link IncorrectAnnotationException}
 *
 * @author ash
 */
public class IncorrectAnnotationExceptionTest {

    private final static Mango mango = new Mango(Config.getDataSource());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() {
        thrown.expect(IncorrectAnnotationException.class);
        thrown.expectMessage("dao interface expected one cc.concurrent.mango.DB annotation but not found");
        mango.create(Dao.class);
    }

    @Test
    public void test2() {
        thrown.expect(IncorrectAnnotationException.class);
        thrown.expectMessage("each method expected one cc.concurrent.mango.SQL annotation but not found");
        Dao2 dao = mango.create(Dao2.class);
        dao.add();
    }

    @Test
    public void test3() {
        thrown.expect(IncorrectAnnotationException.class);
        thrown.expectMessage("if use cache, each method expected one and only one " +
                "cc.concurrent.mango.CacheBy annotation on parameter but found 0");
        Dao3 dao = mango.create(Dao3.class);
        dao.add();
    }

    @Test
    public void test4() {
        thrown.expect(IncorrectAnnotationException.class);
        thrown.expectMessage("if use cache, each method expected one and only one " +
                "cc.concurrent.mango.CacheBy annotation on parameter but found 2");
        Dao3 dao = mango.create(Dao3.class);
        dao.add2(1, 2);
    }


    static interface Dao {
    }

    @DB
    static interface Dao2 {
        public int add();
    }

    @DB
    @Cache(prefix = "dao3_", expire = Day.class)
    static interface Dao3 {
        @SQL("insert into ...")
        public int add();

        @SQL("insert into ...")
        public int add2(@CacheBy int x, @CacheBy int y);
    }

}
