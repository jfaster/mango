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

import cc.concurrent.mango.*;
import cc.concurrent.mango.support.Config;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试{@link IncorrectSqlException}
 *
 * @author ash
 */
public class IncorrectSqlExceptionTest {

    private final static Mango mango = new Mango(Config.getDataSource());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() {
        thrown.expect(IncorrectSqlException.class);
        thrown.expectMessage("if use cache, sql's in clause expected less than or equal 1 but 2");
        Dao dao = mango.create(Dao.class);
        dao.add(new ArrayList<Integer>(), new ArrayList<Integer>());
    }


    @Test
    public void test2() {
        thrown.expect(IncorrectSqlException.class);
        thrown.expectMessage("sql is null or empty");
        Dao dao = mango.create(Dao.class);
        dao.add2();
    }

    @Test
    public void test3() {
        thrown.expect(IncorrectSqlException.class);
        thrown.expectMessage("if use batch update, sql's in clause number expected 0 but 1");
        Dao dao = mango.create(Dao.class);
        dao.batchUpdate2(new ArrayList<Model>());
    }

    @Test
    public void test4() {
        thrown.expect(IncorrectSqlException.class);
        thrown.expectMessage("sql must start with INSERT or DELETE or UPDATE or SELECT");
        Dao dao = mango.create(Dao.class);
        dao.add3();
    }

    @Test
    public void test5() {
        thrown.expect(IncorrectSqlException.class);
        thrown.expectMessage("if use cache, sql's in clause expected less than or equal 1 but 2");
        Dao dao = mango.create(Dao.class);
        dao.gets(new ArrayList<Integer>(), new ArrayList<Integer>());
    }

    @DB
    @Cache(prefix = "dao_", expire = Day.class)
    static interface Dao {
        @SQL("update ... where a in (:1) and b in (:2)")
        public int add(@CacheBy List<Integer> a, List<Integer> b);

        @SQL("")
        public int add2();

        @SQL("update ... where a in (:1.list) and id=:1.id")
        public int[] batchUpdate2(@CacheBy("id") List<Model> models);

        @SQL("test")
        public int add3();

        @SQL("select ... where a in (:1) and b in (:2)")
        public List<Integer> gets(@CacheBy List<Integer> a, List<Integer> b);
    }

    static class Model {
        int id;
        List<Integer> list;

        public int getId() {
            return id;
        }

        public List<Integer> getList() {
            return list;
        }
    }
}
