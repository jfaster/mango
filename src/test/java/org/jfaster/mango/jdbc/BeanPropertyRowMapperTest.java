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

package org.jfaster.mango.jdbc;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import org.jfaster.mango.annotation.*;
import org.jfaster.mango.mapper.MappingException;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Randoms;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.util.logging.MangoLogger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * @author ash
 */
public class BeanPropertyRowMapperTest {

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
    List<MullMsg> msgs = MullMsg.createRandomMsgs(num);
    List<Integer> ids = new ArrayList<Integer>();
    for (MullMsg msg : msgs) {
      int id = dao.insert(msg.getUid(), msg.getYyCon());
      assertThat(id, greaterThan(0));
      msg.setIdxx(id);
      ids.add(id);
    }

    List<MullMsg> dbMsgs = dao.getMsgs(ids);
    assertThat(dbMsgs, hasSize(msgs.size()));
    assertThat(dbMsgs, containsInAnyOrder(msgs.toArray()));
    MullMsg msg = msgs.get(0);
    assertThat(dao.getMsg(msg.getIdxx()), equalTo(msg));
  }

  @Test
  public void test2() {
    MangoLogger.useConsoleLogger();
    MsgDao dao = mango.create(MsgDao.class);
    MullMsg msg = MullMsg.createRandomMsg();
    int id = dao.insert(msg.getUid(), msg.getYyCon());
    assertThat(id, greaterThan(0));
    MullMsg m = dao.getMsg2(id);
    assertThat(m.getMsgItem().getContent(), equalTo(msg.getYyCon()));
  }

  @Test
  public void test3() {
    if (DataSourceConfig.isUseMySQL()) {
      MsgDao dao = mango.create(MsgDao.class);
      MullMsg msg = MullMsg.createRandomMsg();
      int id = dao.insert(msg.getUid(), msg.getYyCon());
      assertThat(id, greaterThan(0));
      MullMsg m = dao.getMsg3(id);
      assertThat(m.getMsgItem().uid, equalTo(msg.getUid()));
    }
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void testException() {
    thrown.expect(MappingException.class);
    boolean old = mango.isCheckColumn();
    mango.setCheckColumn(true);
    Msg2Dao dao = mango.create(Msg2Dao.class);
    MullMsg msg = MullMsg.createRandomMsg();
    int id = dao.insert(msg.getUid(), msg.getYyCon());
    assertThat(id, greaterThan(0));
    try {
      dao.getMsg(id);
    } finally {
      mango.setCheckColumn(old);
    }
  }

  @DB(table = "msg")
  @Results({
      @Result(column = "id", property = "idxx"),
      @Result(column = "content", property = "yyCon")
  })
  interface MsgDao {

    @ReturnGeneratedId
    @SQL("insert into #table(uid, content) values(:1, :2)")
    int insert(int uid, String content);

    @Results({
        @Result(column = "id", property = "idxx"),
        @Result(column = "content", property = "yyCon")
    })
    @SQL("select id, uid, content from #table where id in (:1) order by id")
    public List<MullMsg> getMsgs(List<Integer> ids);

    @SQL("select id, uid, content from #table where id = :1")
    public MullMsg getMsg(int id);

    @Results({
        @Result(column = "content", property = "msgItem.content")
    })
    @SQL("select content from #table where id = :1")
    public MullMsg getMsg2(int id);

    @SQL("select uid as 'msgItem.uid' from #table where id = :1")
    public MullMsg getMsg3(int id);

  }

  @DB(table = "msg")
  interface Msg2Dao {

    @ReturnGeneratedId
    @SQL("insert into #table(uid, content) values(:1, :2)")
    int insert(int uid, String content);

    @SQL("select id, uid, content from #table where id = :1")
    public MullMsg getMsg(int id);

  }

  public static class MullMsg {

    private int idxx;
    private int uid;
    private String yyCon;

    private MsgItem msgItem;

    public static List<MullMsg> createRandomMsgs(int num) {
      List<MullMsg> msgs = new ArrayList<MullMsg>();
      for (int i = 0; i < num; i++) {
        msgs.add(createRandomMsg());
      }
      return msgs;
    }

    public static MullMsg createRandomMsg() {
      MullMsg msg = new MullMsg();
      msg.setUid(Randoms.randomInt(10000));
      msg.setYyCon(Randoms.randomString(20));
      return msg;
    }

    @Override
    public boolean equals(Object obj) {
      if (obj == null)
        return false;
      if (getClass() != obj.getClass())
        return false;
      final MullMsg other = (MullMsg) obj;
      return Objects.equal(this.idxx, other.idxx)
          && Objects.equal(this.uid, other.uid)
          && Objects.equal(this.yyCon, other.yyCon);
    }

    @Override
    public String toString() {
      return MoreObjects.toStringHelper(this).add("idxx", idxx)
          .add("uid", uid).add("yyCon", yyCon).toString();
    }

    public int getIdxx() {
      return idxx;
    }

    public void setIdxx(int idxx) {
      this.idxx = idxx;
    }

    public int getUid() {
      return uid;
    }

    public void setUid(int uid) {
      this.uid = uid;
    }

    public String getYyCon() {
      return yyCon;
    }

    public void setYyCon(String yyCon) {
      this.yyCon = yyCon;
    }

    public MsgItem getMsgItem() {
      return msgItem;
    }

    public void setMsgItem(MsgItem msgItem) {
      this.msgItem = msgItem;
    }
  }

  public static class MsgItem {

    private int uid;

    private String content;

    public int getUid() {
      return uid;
    }

    public void setUid(int uid) {
      this.uid = uid;
    }

    public String getContent() {
      return content;
    }

    public void setContent(String content) {
      this.content = content;
    }
  }


}
