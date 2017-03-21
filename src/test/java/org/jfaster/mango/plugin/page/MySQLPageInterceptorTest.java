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

package org.jfaster.mango.plugin.page;

import org.jfaster.mango.annotation.*;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.operator.cache.Day;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Randoms;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.Msg;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.*;

/**
 * @author ash
 */
public class MySQLPageInterceptorTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();
  private static MsgDao dao;
  {
    Mango mango = Mango.newInstance(ds);
    mango.addInterceptor(new MySQLPageInterceptor());
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

      List<Msg> expected = new ArrayList<Msg>();
      for (int i = 0; i < 10; i++) {
        Msg msg = new Msg();
        msg.setUid(uid);
        msg.setContent(content);
        int id = dao.insert(msg);
        msg.setId(id);
        expected.add(msg);
      }

      List<Msg> actual = new ArrayList<Msg>();

      Page page = Page.create(1, 3);
      List<Msg> msgs = dao.getMsgs(uid, page);
      actual.addAll(msgs);
      assertThat(page.getTotal(), is(10));
      assertThat(msgs.size(), is(3));

      page = Page.create(2, 3);
      msgs = dao.getMsgs(uid, page);
      actual.addAll(msgs);
      assertThat(page.getTotal(), is(10));
      assertThat(msgs.size(), is(3));

      page = Page.create(3, 3);
      msgs = dao.getMsgs(uid, page);
      actual.addAll(msgs);
      assertThat(page.getTotal(), is(10));
      assertThat(msgs.size(), is(3));

      page = Page.create(4, 3);
      msgs = dao.getMsgs(uid, page);
      actual.addAll(msgs);
      assertThat(page.getTotal(), is(10));
      assertThat(msgs.size(), is(1));

      assertThat(actual, equalTo(expected));
    }
  }

  @DB
  interface MsgDao {

    @ReturnGeneratedId
    @SQL("insert into msg(uid, content) values(:uid, :content)")
    public int insert(Msg msg);

    @SQL("select id, uid, content from msg where uid = :1 order by id")
    public List<Msg> getMsgs(int uid, Page page);

  }

}