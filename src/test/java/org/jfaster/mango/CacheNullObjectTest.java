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

import org.jfaster.mango.annotation.*;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.operator.cache.Day;
import org.jfaster.mango.operator.cache.LocalCacheHandler;
import org.jfaster.mango.operator.cache.NullObject;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Randoms;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.User;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

/**
 * @author ash
 */
public class CacheNullObjectTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();

  @Before
  public void before() throws Exception {
    Connection conn = ds.getConnection();
    Table.USER.load(conn);
    conn.close();
  }

  @Test
  public void testSingleKey() throws Exception {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    UserDao dao = mango.create(UserDao.class);
    User user = createRandomUser();
    int id = dao.insert(user);
    String key = getUserKey(id);
    user.setId(id);
    assertThat(cacheHandler.get(key), nullValue());
    assertThat(dao.select(id), equalTo(user));
    assertThat((User) cacheHandler.get(key), equalTo(user));
    assertThat(dao.select(id), equalTo(user));

    user.setName("lulu");
    dao.update(user);
    assertThat(cacheHandler.get(key), nullValue());
    assertThat(dao.select(id), equalTo(user));
    assertThat((User) cacheHandler.get(key), equalTo(user));

    dao.delete(id);
    assertThat(cacheHandler.get(key), nullValue());
    assertThat(dao.select(id), nullValue());

    assertThat((NullObject) cacheHandler.get(key), equalTo(new NullObject()));
    assertThat(dao.select(id), nullValue());
  }

  @Test
  public void testMultiKeysReturnList() throws Exception {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    UserDao dao = mango.create(UserDao.class);

    int base = 10000;

    List<User> users = createRandomUsers(5);
    List<Integer> ids = new ArrayList<Integer>();
    for (User user : users) {
      int id = dao.insert(user);
      user.setId(id);
      ids.add(id);
    }
    for (int i = 0; i < users.size(); i++) {
      ids.add(base + i);
    }

    List<User> actual = dao.getUserList(ids);
    assertThat(actual, hasSize(users.size()));
    assertThat(actual, containsInAnyOrder(users.toArray()));
    for (int i = 0; i < users.size(); i++) {
      Integer id = ids.get(i);
      User user = users.get(i);
      assertThat((User) cacheHandler.get(getUserKey(id)), equalTo(user));
    }
    for (int i = users.size(); i < ids.size(); i++) {
      assertThat((NullObject) cacheHandler.get(getUserKey(ids.get(i))), equalTo(new NullObject()));
    }
    actual = dao.getUserList(ids);
    assertThat(actual, hasSize(users.size()));
    assertThat(actual, containsInAnyOrder(users.toArray()));
    cacheHandler.delete(getUserKey(users.get(0).getId()));
    actual = dao.getUserList(ids);
    assertThat(actual, hasSize(users.size()));
    assertThat(actual, containsInAnyOrder(users.toArray()));
  }


  @DB
  @Cache(prefix = "user", expire = Day.class, cacheNullObject = true)
  interface UserDao {

    @ReturnGeneratedId
    @CacheIgnored
    @SQL("insert into user(name, age, gender, money, update_time) values(:1.name, :1.age, :1.gender, :1.money, :1.updateTime)")
    int insert(User user);

    @SQL("delete from user where id=:1")
    public int delete(@CacheBy int id);

    @SQL("update user set name=:1.name, age=:1.age, gender=:1.gender, money=:1.money, update_time=:1.updateTime where id=:1.id")
    public int update(@CacheBy("id") User user);

    @SQL("select id, name, age, gender, money, update_time from user where id=:1")
    public User select(@CacheBy() int id);

    @SQL("select id, name, age, gender, money, update_time from user where id in (:1)")
    public List<User> getUserList(@CacheBy() List<Integer> ids);

  }

  private String getUserKey(int id) {
    return "user_" + id;
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

  private List<User> createRandomUsers(int size) {
    List<User> users = new ArrayList<User>(size);
    for (int i = 0; i < size; i++) {
      users.add(createRandomUser());
    }
    return users;
  }

}
