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

package org.jfaster.mango.exception;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.Config;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试${@link IncorrectReturnTypeException}
 *
 * @author ash
 */
public class IncorrectReturnTypeExceptionTest {

    private final static Mango mango = new Mango(Config.getDataSource());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() {
        thrown.expect(IncorrectReturnTypeException.class);
        thrown.expectMessage("if sql don't start with select, " +
                "update return type expected int, " +
                "batch update return type expected int[], " +
                "but void");
        Dao dao = mango.create(Dao.class);
        dao.update();
    }

    @Test
    public void test2() {
        thrown.expect(IncorrectReturnTypeException.class);
        thrown.expectMessage("if sql has in clause, return type " +
                "expected array or implementations of java.util.List or implementations of java.util.Set " +
                "but int");
        Dao dao = mango.create(Dao.class);
        dao.gets(new ArrayList<Integer>());
    }

    @DB
    static interface Dao {
        @SQL("update ...")
        public void update();

        @SQL("select * from table where ids in (:1)")
        public int gets(List<Integer> ids);
    }

}
