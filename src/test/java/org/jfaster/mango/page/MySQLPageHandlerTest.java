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

package org.jfaster.mango.page;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.ReturnGeneratedId;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.Msg;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class MySQLPageHandlerTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();
  private static MsgDao dao;
  {
    Mango mango = Mango.newInstance(ds);
    dao = mango.create(MsgDao.class);
  }

  @Before
  public void before() throws Exception {
    Table.MSG.load(ds);
  }

  @Test
  public void interceptQuery() throws Exception {
    if (DataSourceConfig.isUseMySQL()) {
      int uid = 100;
      String content = "hi";

      List<Msg> expected = new ArrayList<>();
      for (int i = 0; i < 10; i++) {
        Msg msg = new Msg();
        msg.setUid(uid);
        msg.setContent(content);
        int id = dao.insert(msg);
        msg.setId(id);
        expected.add(msg);
      }

      List<Msg> actual = new ArrayList<>();

      List<Msg> msgs = dao.getMsgs(uid, Page.of(0, 3, Direction.ASC, "id"));
      actual.addAll(msgs);
      assertThat(msgs.size(), is(3));

      msgs = dao.getMsgs(uid, Page.of(1, 3, Direction.ASC, "id"));
      actual.addAll(msgs);
      assertThat(msgs.size(), is(3));

      PageResult<Msg> pr = dao.getMsgs2(uid, Page.of(2, 3, Direction.ASC, "id"));
      actual.addAll(pr.getData());
      assertThat(pr.getTotal(), is(10));
      assertThat(pr.getData().size(), is(3));

      pr = dao.getMsgs2(uid, Page.of(3, 3, Direction.ASC, "id"));
      actual.addAll(pr.getData());
      assertThat(pr.getTotal(), is(10));
      assertThat(pr.getData().size(), is(1));

      assertThat(actual, equalTo(expected));
    }
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void interceptQuery2() throws Exception {
    if (DataSourceConfig.isUseMySQL()) {
      thrown.expect(IllegalArgumentException.class);
      thrown.expectMessage("Parameter page is null");
      dao.getMsgs(100, null);
    }
  }

  @DB
  interface MsgDao {

    @ReturnGeneratedId
    @SQL("insert into msg(uid, content) values(:uid, :content)")
    int insert(Msg msg);

    @SQL("select id, uid, content from msg where uid = :1")
    List<Msg> getMsgs(int uid, Page page);

    @SQL("select id, uid, content from msg where uid = :1")
    PageResult<Msg> getMsgs2(int uid, Page page);
  }

}