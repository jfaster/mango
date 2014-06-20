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

package cc.concurrent.mango;

import cc.concurrent.mango.model4table.Msg;
import cc.concurrent.mango.model4table.Tables;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

/**
 * 批量更新测试
 *
 * @author ash
 */
public class ShardingBatchUpdateTest {

    private final static DataSource ds = Config.getDataSource();
    private final static Mango mango = new Mango(ds);
    private final static MsgDao dao = mango.create(MsgDao.class);

    @Before
    public void before() throws Exception {
        Connection conn = ds.getConnection();
        Tables.SHARDING_MSG.load(conn);
        conn.close();
    }

    @Test
    public void testBatchInsertWithSharding() {
        int size = 30;
        String content = "content";
        List<Msg> msgs = new ArrayList<Msg>();
        for (int uid = 0; uid < size; uid++) {
            Msg msg = new Msg();
            msg.setUid(uid);
            msg.setContent(content);
            msgs.add(msg);
        }
        int[] r = dao.batchInsert(msgs);
        assertThat(r.length, equalTo(size));
        for (int i : r) {
            assertThat(i, equalTo(1));
        }
        for (int uid = 0; uid < size; uid++) {
            msgs = dao.getMsgsByUid(uid);
            assertThat(msgs, hasSize(1));
            assertThat(msgs.get(0).getUid(), equalTo(uid));
            assertThat(msgs.get(0).getContent(), equalTo(content));
        }
    }

    @Test
    public void testBatchInsertNoSharding() {
        int size = 30;
        int uid = 1;
        String content = "content";
        List<Msg> msgs = new ArrayList<Msg>();
        for (int i = 0; i < size; i++) {
            Msg msg = new Msg();
            msg.setUid(uid);
            msg.setContent(content);
            msgs.add(msg);
        }
        int[] r = dao.batchInsert(msgs);
        for (int i : r) {
            assertThat(i, equalTo(1));
        }
        msgs = dao.getMsgsByUid(uid);
        assertThat(msgs, hasSize(size));
        for (Msg msg : msgs) {
            assertThat(msg.getUid(), equalTo(uid));
            assertThat(msg.getContent(), equalTo(content));
        }
    }

    @DB(table = "msg_")
    interface MsgDao {

        @SQL("insert into ${:table+:m.uid%10}(uid, content) values(:m.uid, :m.content)")
        public int[] batchInsert(@Rename("m") List<Msg> msgs);

        @SQL("select id, uid, content from ${:table+:1%10} where uid=:1")
        public List<Msg> getMsgsByUid(int uid);

    }

}
