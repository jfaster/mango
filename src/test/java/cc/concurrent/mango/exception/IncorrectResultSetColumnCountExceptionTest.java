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
import cc.concurrent.mango.model4table.Person;
import cc.concurrent.mango.model4table.Tables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * 测试{@link IncorrectResultSetColumnCountException}
 *
 * @author ash
 */
public class IncorrectResultSetColumnCountExceptionTest {

    private final static DataSource ds = Config.getDataSource();
    private final static Mango mango = new Mango(ds);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() throws Exception {
        Connection conn = ds.getConnection();
        Tables.PERSON.load(conn);
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















