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

package org.jfaster.mango.stat;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.ReturnGeneratedId;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Randoms;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.User;
import org.jfaster.mango.util.logging.MangoLogger;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * @author ash
 */
public class SimpleStatMonitorTest {

  @Test
  public void test() throws Exception {
    MangoLogger.useNoLogger();
    DataSource ds = DataSourceConfig.getDataSource();
    Table.USER.load(ds);
    Mango mango = Mango.newInstance(ds);
    mango.setStatMonitor(new SimpleStatMonitor(1));
    UserDao dao = mango.create(UserDao.class);
    int id = dao.insertUser(createRandomUser());
    long end = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(5);
    while (System.currentTimeMillis() < end) {
      dao.getName(id);
    }
    mango.shutDownStatMonitor();
  }

  @DB(table = "user")
  static interface UserDao {

    @SQL("select name from #table where id = :1")
    public String getName(int id);

    @ReturnGeneratedId
    @SQL("insert into user(name, age, gender, money, update_time) " +
        "values(:1.name, :1.age, :1.gender, :1.money, :1.updateTime)")
    public int insertUser(User user);

  }

  private static User createRandomUser() {
    Random r = new Random();
    String name = Randoms.randomString(20);
    int age = r.nextInt(200);
    boolean gender = r.nextBoolean();
    long money = r.nextInt(1000000);
    Date date = new Date();
    User user = new User(name, age, gender, money, date);
    return user;
  }

}
