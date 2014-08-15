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

package org.jfaster.mango.transaction;

import org.jfaster.mango.DB;
import org.jfaster.mango.Mango;
import org.jfaster.mango.ReturnGeneratedId;
import org.jfaster.mango.SQL;
import org.jfaster.mango.support.Config;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.Msg;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * 测试事务
 *
 * @author ash
 */
public class TransactionTest {

    private final static DataSource ds = Config.getDataSource();
    private final static Mango mango = new Mango(ds);
    private final static MsgDao dao = mango.create(MsgDao.class);

    @Before
    public void before() throws Exception {
        Connection conn = ds.getConnection();
        Table.MSG.load(conn);
        conn.close();
    }

    @Test
    public void test() throws Exception {
        Msg msg = new Msg();
        msg.setUid(100);
        msg.setContent("test");
        int id = dao.insert(msg);
        msg.setId(id);
        Transaction tx = TransactionFactory.newTransaction(TransactionIsolationLevel.SERIALIZABLE);
        try {
            msg.setContent("test2");
            dao.update(msg);
            tx.commit();
        } catch (Throwable t) {
            tx.rollback();
        }

        assertThat(dao.getMsgs(id), equalTo(msg));
    }

//    private void checkConn(boolean autoCommit, int level) throws Exception {
//        Connection conn = ds.getConnection();
//        assertThat(conn.getAutoCommit(), is(autoCommit));
//        assertThat(conn.getTransactionIsolation(), is(level));
//        conn.close();
//    }


    @DB
    interface MsgDao {

        @ReturnGeneratedId
        @SQL("insert into msg(uid, content) values(:1.uid, :1.content)")
        public int insert(Msg msg);

        @SQL("update msg set content=:1.content where id=:1.id and uid=:1.uid")
        public int update(Msg msg);

        @SQL("select id, uid, content from msg where id=:1")
        public Msg getMsgs(int id);

    }

}
