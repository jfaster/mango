/*
 * Copyright 2014 mango.concurrent.cc
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

package cc.concurrent.mango.exception;

import cc.concurrent.mango.support.Config;
import cc.concurrent.mango.DB;
import cc.concurrent.mango.Mango;
import cc.concurrent.mango.SQL;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * 测试{@link NotReadableParameterException}
 *
 * @author ash
 */
public class NotReadableParameterExceptionTest {

    private final static Mango mango = new Mango(Config.getDataSource());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() {
        thrown.expect(NotReadableParameterException.class);
        thrown.expectMessage("parameter :1 is not readable");
        Dao dao = mango.create(Dao.class);
        dao.add();
    }

    @Test
    public void test2() {
        thrown.expect(NotReadableParameterException.class);
        thrown.expectMessage("parameter :1 is not readable");
        Dao dao = mango.create(Dao.class);
        dao.gets();
    }

    @DB
    static interface Dao {
        @SQL("insert into user(uid) values(:1)")
        public int add();

        @SQL("select uid from user where uid in (:1)")
        public int[] gets();
    }

}
