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
import org.jfaster.mango.support.model4table.BT;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * 测试字段中含有boolean类型
 *
 * @author ash
 */
public class BTTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();
  private final static Mango mango = Mango.newInstance(ds);
  private final static BTDao dao = mango.create(BTDao.class);

  @Before
  public void before() throws Exception {
    Connection conn = ds.getConnection();
    Table.BT.load(conn);
    conn.close();
  }

  @Test
  public void test() {
    BT bt = new BT();
    bt.setOk(true);
    int id = dao.insert(bt);
    assertThat(dao.getBT(id).isOk(), equalTo(true));
    bt.setOk(false);
    id = dao.insert(bt);
    assertThat(dao.getBT(id).isOk(), equalTo(false));
  }


  @DB(table = "bt")
  interface BTDao {

    @ReturnGeneratedId
    @SQL("insert into #table(is_ok) values(:isOk)")
    public int insert(BT bt);

    @SQL("select id, is_ok from #table where id=:1")
    public BT getBT(int id);

  }

}
