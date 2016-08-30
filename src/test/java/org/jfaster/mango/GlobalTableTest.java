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

package org.jfaster.mango;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.ReturnGeneratedId;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.Msg;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 测试全局表
 *
 * @author ash
 */
public class GlobalTableTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();
  private final static Mango mango = Mango.newInstance(ds);

  @Before
  public void before() throws Exception {
    Connection conn = ds.getConnection();
    Table.MSG.load(conn);
    conn.close();
  }

  @Test
  public void test() {
    MsgDao dao = mango.create(MsgDao.class);
    int num = 5;
    List<Msg> msgs = Msg.createRandomMsgs(num);
    List<Integer> ids = new ArrayList<Integer>();
    for (Msg msg : msgs) {
      int id = dao.insert(msg.getUid(), msg.getContent());
      assertThat(id, greaterThan(0));
      msg.setId(id);
      ids.add(id);
    }

    // 插入后数据一致
    List<Msg> dbMsgs = dao.getMsgs(ids);
    assertThat(dbMsgs, hasSize(msgs.size()));
    assertThat(dbMsgs, containsInAnyOrder(msgs.toArray()));

    // 更新后数据一致
    Msg msg = msgs.get(0);
    Msg randomMsg = Msg.createRandomMsg();
    msg.setUid(randomMsg.getUid());
    msg.setContent(randomMsg.getContent());
    dao.update(msg);
    dbMsgs = dao.getMsgs(ids);
    assertThat(dbMsgs, hasSize(msgs.size()));
    assertThat(dbMsgs, containsInAnyOrder(msgs.toArray()));

    // 删除后数据一致
    dao.deleteMsgs(ids);
    dbMsgs = dao.getMsgs(ids);
    assertThat(dbMsgs, hasSize(0));
  }

  @DB(table = "msg")
  interface MsgDao {

    @ReturnGeneratedId
    @SQL("insert into #table(uid, content) values(:1, :2)")
    int insert(int uid, String content);

    @SQL("update #table set uid=:1.uid, content=:1.content where id=:1.id")
    public int update(Msg msg);

    @SQL("select id, uid, content from #table where id in (:1) order by id")
    public List<Msg> getMsgs(List<Integer> ids);

    @SQL("delete from #table where id=:1")
    public int[] deleteMsgs(List<Integer> ids);

  }

}
