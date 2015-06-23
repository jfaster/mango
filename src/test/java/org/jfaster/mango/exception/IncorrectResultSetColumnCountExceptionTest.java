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
import org.jfaster.mango.exception.jdbc.IncorrectResultSetColumnCountException;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.Person;
import org.jfaster.mango.support.Config;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 测试{@link org.jfaster.mango.exception.jdbc.IncorrectResultSetColumnCountException}
 *
 * @author ash
 */
public class IncorrectResultSetColumnCountExceptionTest {

    private final static DataSource ds = Config.getDataSource();
    private final static Mango mango = Mango.newInstance(ds);
    static {
        mango.setDefaultLazyInit(true);
    }

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() throws Exception {
        Connection conn = ds.getConnection();
        Table.PERSON.load(conn);
        conn.close();
    }

    @Test
    public void test() {
        thrown.expect(IncorrectResultSetColumnCountException.class);
        thrown.expectMessage("incorrect column count, expected 1 but 2");
        PersonDao dao = mango.create(PersonDao.class);
        int id = 1;
        dao.add(new Person(id, "ash"));
        dao.get(id);
    }

    @DB
    static interface PersonDao {

        @SQL("insert into person(id, name) values(:1.id, :1.name)")
        public int add(Person p);

        @SQL("select id, name from person where id=:1")
        public int get(int id);
    }

}















