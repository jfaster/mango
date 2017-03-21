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

package org.jfaster.mango.interceptor;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.ReturnGeneratedId;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.binding.BoundSql;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Randoms;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.User;
import org.jfaster.mango.util.jdbc.SQLType;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

/**
 * @author ash
 */
public class InterceptorSQLTypeTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();

  @Before
  public void before() throws Exception {
    Table.USER.load(ds);
  }

  @Test
  public void test() throws Exception {
    final AtomicInteger t = new AtomicInteger();

    Mango mango = Mango.newInstance(ds);
    mango.addInterceptor(new UpdateInterceptor() {
      @Override
      public void interceptUpdate(BoundSql boundSql, List<Parameter> parameters, SQLType sqlType, DataSource dataSource) {
        t.incrementAndGet();
        assertThat(sqlType, equalTo(SQLType.INSERT));
      }
    });
    UserDao dao = mango.create(UserDao.class);
    User user = createRandomUser();
    int id = dao.addUser(user);
    assertThat(t.intValue(), equalTo(1));

    mango = Mango.newInstance(ds);
    mango.addInterceptor(new QueryInterceptor() {
      @Override
      public void interceptQuery(BoundSql boundSql, List<Parameter> parameters, DataSource dataSource) {
        t.incrementAndGet();
      }
    });
    dao = mango.create(UserDao.class);
    dao.getUser(id);
    assertThat(t.intValue(), equalTo(2));

    mango = Mango.newInstance(ds);
    mango.addInterceptor(new UpdateInterceptor() {
      @Override
      public void interceptUpdate(BoundSql boundSql, List<Parameter> parameters, SQLType sqlType, DataSource dataSource) {
        t.incrementAndGet();
        assertThat(sqlType, equalTo(SQLType.UPDATE));
      }
    });
    dao = mango.create(UserDao.class);
    user = createRandomUser();
    user.setId(id);
    dao.updateUser(user);
    assertThat(t.intValue(), equalTo(3));

    mango = Mango.newInstance(ds);
    mango.addInterceptor(new UpdateInterceptor() {
      @Override
      public void interceptUpdate(BoundSql boundSql, List<Parameter> parameters, SQLType sqlType, DataSource dataSource) {
        t.incrementAndGet();
        assertThat(sqlType, equalTo(SQLType.DELETE));
      }
    });
    dao = mango.create(UserDao.class);
    dao.deleteUser(id);
    assertThat(t.intValue(), equalTo(4));

    mango = Mango.newInstance(ds);
    mango.addInterceptor(new UpdateInterceptor() {
      @Override
      public void interceptUpdate(BoundSql boundSql, List<Parameter> parameters, SQLType sqlType, DataSource dataSource) {
        t.incrementAndGet();
        assertThat(sqlType, equalTo(SQLType.REPLACE));
      }
    });
    dao = mango.create(UserDao.class);
    user = createRandomUser();
    user.setId(id);
    try {
      dao.replaceUser(user);
    } catch (Exception e) {
    }
    assertThat(t.intValue(), equalTo(5));

    mango = Mango.newInstance(ds);
    mango.addInterceptor(new UpdateInterceptor() {
      @Override
      public void interceptUpdate(BoundSql boundSql, List<Parameter> parameters, SQLType sqlType, DataSource dataSource) {
        t.incrementAndGet();
        assertThat(sqlType, equalTo(SQLType.MERGE));
      }
    });
    dao = mango.create(UserDao.class);
    user = createRandomUser();
    user.setId(id);
    try {
      dao.mergeUser(user);
    } catch (Exception e) {
    }
    assertThat(t.intValue(), equalTo(6));

    mango = Mango.newInstance(ds);
    mango.addInterceptor(new UpdateInterceptor() {
      @Override
      public void interceptUpdate(BoundSql boundSql, List<Parameter> parameters, SQLType sqlType, DataSource dataSource) {
        t.incrementAndGet();
        assertThat(sqlType, equalTo(SQLType.TRANCATE));
      }
    });
    dao = mango.create(UserDao.class);
    try {
      dao.truncate();
    } catch (Exception e) {
    }
    assertThat(t.intValue(), equalTo(7));
  }

  @DB
  interface UserDao {

    @ReturnGeneratedId
    @SQL("insert into user(name, age, gender, money, update_time) " +
        "values(:name, :age, :gender, :money, :updateTime)")
    int addUser(User user);

    @SQL("select id, name, age, gender, money, update_time from user where id = :1")
    User getUser(int id);

    @SQL("update user set name=:name, age=:age, gender=:gender, money=:money, update_time=:updateTime where id=:id")
    void updateUser(User user);

    @SQL("delete from user where id = :1")
    void deleteUser(int id);

    @SQL("replace into user(name, age, gender, money, update_time) " +
        "values(:name, :age, :gender, :money, :updateTime)")
    void replaceUser(User user);

    @SQL("merge into user(name, age, gender, money, update_time) " +
        "values(:name, :age, :gender, :money, :updateTime)")
    void mergeUser(User user);

    @SQL("truncate user")
    void truncate();

  }

  private User createRandomUser() {
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
