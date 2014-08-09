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

import org.jfaster.mango.support.Config;
import org.jfaster.mango.DB;
import org.jfaster.mango.Mango;
import org.jfaster.mango.SQL;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试{@link IncorrectParameterCountException}
 *
 * @author ash
 */
public class IncorrectParameterCountExceptionTest {

    private final static Mango mango = new Mango(Config.getDataSource());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() {
        thrown.expect(IncorrectParameterCountException.class);
        thrown.expectMessage("batch update expected one and only one parameter but 2");
        Dao dao = mango.create(Dao.class);
        dao.batchAdd(new ArrayList<Integer>(), 1);
    }

    @DB
    static interface Dao {
        @SQL("insert into ...")
        public int[] batchAdd(List<Integer> list, int a);
    }


}
