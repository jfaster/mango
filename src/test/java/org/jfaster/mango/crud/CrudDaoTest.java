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

package org.jfaster.mango.crud;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.Msg;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.nullValue;

/**
 * @author ash
 */
public class CrudDaoTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();
  private final static Mango mango = Mango.newInstance(ds);
  private final static MsgDao dao = mango.create(MsgDao.class);

  @Before
  public void before() throws Exception {
    Table.MSG.load(ds);
  }

  @Test
  public void test() throws Exception {
    Msg msg = Msg.createRandomMsg();
    int id = dao.addAndReturnGeneratedId(msg);
    msg.setId(id);
    assertThat(dao.getOne(id), equalTo(msg));
    Msg msg2 = Msg.createRandomMsg();
    dao.add(msg2);
    Msg msg3 = Msg.createRandomMsg();
    int id3 = dao.addAndReturnGeneratedId(msg3);
    msg3.setId(id3);
    assertThat(dao.getOne(id3), equalTo(msg3));
    List<Integer> ids = Lists.newArrayList(id, id3);
    List<Msg> msgs = dao.getMulti(ids);
    assertThat(msgs.size(), equalTo(2));
    Map<Integer, Msg> mapping = Maps.newHashMap();
    mapping.put(id, msg);
    mapping.put(id3, msg3);
    for (Msg actualMsg : msgs) {
      assertThat(actualMsg, equalTo(mapping.get(actualMsg.getId())));
    }

    msg.setContent("ash");
    int r = dao.update(msg);
    assertThat(r, equalTo(1));
    assertThat(dao.getOne(id), equalTo(msg));
    msgs = Lists.newArrayList(msg, msg3);
    int[] rr = dao.update(msgs);
    assertThat(rr, equalTo(new int[] {1, 1}));

    msg.setId(-1);
    msg3.setId(-3);
    r = dao.update(msg);
    assertThat(r, equalTo(0));
    rr = dao.update(msgs);
    assertThat(rr, equalTo(new int[] {0, 0}));

    dao.delete(id);
    assertThat(dao.getOne(id), nullValue());
    msgs = Msg.createRandomMsgs(5);
    dao.add(msgs);
  }

  @DB(table = "msg")
  interface MsgDao extends CrudDao<Msg, Integer> {
  }

}