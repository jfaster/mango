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
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

/**
 * 测试生成long类型的自增id
 *
 * @author ash
 */
public class GeneratedLongTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();
  private final static Mango mango = Mango.newInstance(ds);
  private final static MsgDao dao = mango.create(MsgDao.class);

  @Before
  public void before() throws Exception {
    Connection conn = ds.getConnection();
    Table.LONG_ID_MSG.load(conn);
    conn.close();
  }

  @Test
  public void test() {
    int uid = 100;
    String content = "content";
    long id = dao.insert(uid, content);
    assertThat(id, greaterThan((long) Integer.MAX_VALUE));
    long id2 = dao.insert(uid, content);
    assertThat(id2, equalTo(id + 1));
  }

  @DB()
  interface MsgDao {

    @ReturnGeneratedId
    @SQL("insert into long_id_msg(uid, content) values(:1, :2)")
    long insert(int uid, String content);

  }

}
