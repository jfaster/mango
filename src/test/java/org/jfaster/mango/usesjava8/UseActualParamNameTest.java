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

package org.jfaster.mango.usesjava8;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.User;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;

/**
 * @author ash
 */
public class UseActualParamNameTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();
  private final static Mango mango = Mango.newInstance(ds);
  {
    mango.setUseActualParamName(true);
  }

  @Before
  public void before() throws Exception {
    Table.USER.load(ds);
  }

  @Test
  public void test() throws Exception {
    UserDao dao = mango.create(UserDao.class);
    dao.getUser(1);
  }

  @DB()
  static interface UserDao {

    @SQL("select id, name, age, gender, money, update_time from user where id = :id")
    public User getUser(int id);

  }

}
