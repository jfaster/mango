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

import cc.concurrent.mango.Config;
import cc.concurrent.mango.DB;
import cc.concurrent.mango.Mango;
import cc.concurrent.mango.SQL;
import cc.concurrent.mango.model4table.Msg;
import cc.concurrent.mango.model4table.Tables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

/**
 * 测试java.lang包下的常规异常
 *
 * @author ash
 */
public class JdkExceptionTest {

    private final static DataSource ds = Config.getDataSource();
    private final static Mango mango = new Mango(ds);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() throws Exception {
        Connection conn = ds.getConnection();
        Tables.MSG.load(conn);
        conn.close();
    }

    @Test
    public void testBatchUpdateParameterNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("batchUpdate's parameter can't be null");
        MsgDao dao = mango.create(MsgDao.class);
        dao.batchInsert(null);
    }

    @Test
    public void testBatchUpdateParameterEmpty() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("batchUpdate's parameter can't be empty");
        MsgDao dao = mango.create(MsgDao.class);
        dao.batchInsert(new ArrayList<Msg>());
    }

    @Test
    public void testIterableParameterNull() {
        thrown.expect(NullPointerException.class);
        thrown.expectMessage("value of :1 can't be null");
        MsgDao dao = mango.create(MsgDao.class);
        dao.getMsgs(null);
    }

    @Test
    public void testIterableParameterEmpty() {
        thrown.expect(IllegalArgumentException.class);
        thrown.expectMessage("value of :1 can't be empty");
        MsgDao dao = mango.create(MsgDao.class);
        dao.getMsgs(new ArrayList<Integer>());
    }

    @DB
    static interface MsgDao {

        @SQL("insert into msg(uid, content) values(:1.uid, :1.content)")
        public int[] batchInsert(List<Msg> msgs);

        @SQL("select id, uid, content from msg where id in (:1)")
        public List<Msg> getMsgs(List<Integer> ids);

    }



}
