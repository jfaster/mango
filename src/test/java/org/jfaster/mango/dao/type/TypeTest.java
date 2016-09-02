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

package org.jfaster.mango.dao.type;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.ReturnGeneratedId;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.BT;
import org.jfaster.mango.support.model4table.TableIncludeAllTypes;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.Arrays;

import static org.hamcrest.CoreMatchers.equalTo;

/**
 * @author ash
 */
public class TypeTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();
  private final static Mango mango = Mango.newInstance(ds);
  private final static TableIncludeAllTypesDao dao = mango.create(TableIncludeAllTypesDao.class);

  @Before
  public void before() throws Exception {
    Connection conn = ds.getConnection();
    Table.TABLE_INCLUDE_ALL_TYPES.load(conn);
    conn.close();
  }

  @Test
  public void test() {
    TableIncludeAllTypes t = new TableIncludeAllTypes();
    t.setUid(100);
    t.setContent(new byte[] {1, 2});
    int id = dao.add(t);
    System.out.println(Arrays.toString(dao.getById(id).getContent()));
  }

  @DB(table = "table_include_all_types")
  interface TableIncludeAllTypesDao {

    @ReturnGeneratedId
    @SQL("insert into #table(uid, content) values(:uid, :content)")
    int add(TableIncludeAllTypes tableIncludeAllTypes);

    @SQL("select id, uid, content from #table where id = :1")
    TableIncludeAllTypes getById(int id);

  }


}
