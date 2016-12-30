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

import com.google.common.collect.Lists;
import org.jfaster.mango.annotation.*;
import org.jfaster.mango.operator.Config;
import org.jfaster.mango.operator.Mango;
import org.jfaster.mango.operator.cache.Day;
import org.jfaster.mango.operator.cache.LocalCacheHandler;
import org.jfaster.mango.support.DataSourceConfig;
import org.jfaster.mango.support.Randoms;
import org.jfaster.mango.support.Table;
import org.jfaster.mango.support.model4table.Msg;
import org.jfaster.mango.support.model4table.User;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 测试cache
 *
 * @author ash
 */
public class CacheTest {

  private final static DataSource ds = DataSourceConfig.getDataSource();

  @Before
  public void before() throws Exception {
    Connection conn = ds.getConnection();
    Table.USER.load(conn);
    Table.MSG.load(conn);
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
  }

  @Test
  public void testMultiKeysReturnList() throws Exception {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    UserDao dao = mango.create(UserDao.class);
    List<User> users = createRandomUsers(5);
    List<Integer> ids = new ArrayList<Integer>();
    for (User user : users) {
      int id = dao.insert(user);
      user.setId(id);
      ids.add(id);
    }
    List<User> actual = dao.getUserList(ids);
    assertThat(actual, hasSize(users.size()));
    assertThat(actual, containsInAnyOrder(users.toArray()));
    for (int i = 0; i < ids.size(); i++) {
      Integer id = ids.get(i);
      User user = users.get(i);
      assertThat((User) cacheHandler.get(getUserKey(id)), equalTo(user));
    }
    actual = dao.getUserList(ids);
    assertThat(actual, hasSize(users.size()));
    assertThat(actual, containsInAnyOrder(users.toArray()));
    cacheHandler.delete(getUserKey(users.get(0).getId()));
    actual = dao.getUserList(ids);
    assertThat(actual, hasSize(users.size()));
    assertThat(actual, containsInAnyOrder(users.toArray()));
  }

  @Test
  public void testMultiKeysReturnSet() throws Exception {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    UserDao dao = mango.create(UserDao.class);
    List<User> users = createRandomUsers(5);
    List<Integer> ids = new ArrayList<Integer>();
    for (User user : users) {
      int id = dao.insert(user);
      user.setId(id);
      ids.add(id);
    }
    Set<User> actual = dao.getUserSet(ids);
    assertThat(actual, hasSize(users.size()));
    assertThat(actual, containsInAnyOrder(users.toArray()));
    for (int i = 0; i < ids.size(); i++) {
      Integer id = ids.get(i);
      User user = users.get(i);
      assertThat((User) cacheHandler.get(getUserKey(id)), equalTo(user));
    }
    actual = dao.getUserSet(ids);
    assertThat(actual, hasSize(users.size()));
    assertThat(actual, containsInAnyOrder(users.toArray()));
    cacheHandler.delete(getUserKey(users.get(0).getId()));
    actual = dao.getUserSet(ids);
    assertThat(actual, hasSize(users.size()));
    assertThat(actual, containsInAnyOrder(users.toArray()));
  }

  @Test
  public void testMultiKeysReturnArray() throws Exception {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    UserDao dao = mango.create(UserDao.class);
    List<User> users = createRandomUsers(5);
    List<Integer> ids = new ArrayList<Integer>();
    for (User user : users) {
      int id = dao.insert(user);
      user.setId(id);
      ids.add(id);
    }
    List<User> actual = Arrays.asList(dao.getUserArray(ids));
    assertThat(actual, hasSize(users.size()));
    assertThat(actual, containsInAnyOrder(users.toArray()));
    for (int i = 0; i < ids.size(); i++) {
      Integer id = ids.get(i);
      User user = users.get(i);
      assertThat((User) cacheHandler.get(getUserKey(id)), equalTo(user));
    }
    actual = Arrays.asList(dao.getUserArray(ids));
    assertThat(actual, hasSize(users.size()));
    assertThat(actual, containsInAnyOrder(users.toArray()));
    cacheHandler.delete(getUserKey(users.get(0).getId()));
    actual = Arrays.asList(dao.getUserArray(ids));
    assertThat(actual, hasSize(users.size()));
    assertThat(actual, containsInAnyOrder(users.toArray()));
  }

  @Test
  public void testSingleKeyReturnList() throws Exception {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    List<Msg> msgs = new ArrayList<Msg>();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    MsgDao dao = mango.create(MsgDao.class);
    int uid = 100;
    String key = getMsgKey(uid);

    for (int i = 0; i < 3; i++) {
      Msg msg = createRandomMsg(uid);
      msgs.add(msg);
      msg.setId(dao.insert(msg));
      assertThat(cacheHandler.get(key), nullValue());
      List<Msg> actual = dao.getMsgs(uid);
      assertThat(actual, hasSize(msgs.size()));
      assertThat(actual, contains(msgs.toArray()));
      @SuppressWarnings("unchecked")
      List<Msg> cacheActual = (List<Msg>) cacheHandler.get(key);
      assertThat(cacheActual, hasSize(msgs.size()));
      assertThat(cacheActual, contains(msgs.toArray()));
    }

    List<Msg> actual = dao.getMsgs(uid);
    assertThat(actual, hasSize(msgs.size()));
    assertThat(actual, contains(msgs.toArray()));

    Msg msg = msgs.get(0);
    msg.setContent("ash");
    dao.update(msg);
    assertThat(cacheHandler.get(key), nullValue());

    actual = dao.getMsgs(uid);
    assertThat(actual, hasSize(msgs.size()));
    assertThat(actual, contains(msgs.toArray()));

    msg = msgs.remove(0);
    dao.delete(msg.getUid(), msg.getId());
    actual = dao.getMsgs(uid);
    assertThat(actual, hasSize(msgs.size()));
    assertThat(actual, contains(msgs.toArray()));
  }

  @SuppressWarnings("unchecked")
  @Test
  public void testSingleKeyReturnList2() throws Exception {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    List<Msg> msgs = new ArrayList<Msg>();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    MsgDao dao = mango.create(MsgDao.class);
    int uid = 100;
    String key = getMsgKey(uid);

    List<Msg> actual = dao.getMsgs(uid);
    assertThat(actual, hasSize(0));
    List<Msg> cacheActual = (List<Msg>) cacheHandler.get(key);
    assertThat(cacheActual, hasSize(0));

    Msg msg = createRandomMsg(uid);
    msgs.add(msg);
    msg.setId(dao.insert(msg));

    actual = dao.getMsgs(uid);
    assertThat(actual, hasSize(msgs.size()));
    assertThat(actual, contains(msgs.toArray()));
    cacheActual = (List<Msg>) cacheHandler.get(key);
    assertThat(cacheActual, hasSize(msgs.size()));
    assertThat(cacheActual, contains(msgs.toArray()));


    msg = msgs.remove(0);
    dao.delete(msg.getUid(), msg.getId());
    actual = dao.getMsgs(uid);
    assertThat(actual, hasSize(0));
    cacheActual = (List<Msg>) cacheHandler.get(key);
    assertThat(cacheActual, hasSize(0));
  }


  @Test
  public void testUpdateWithInStatement() {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    UserDao dao = mango.create(UserDao.class);
    List<User> users = createRandomUsers(5);
    List<Integer> ids = new ArrayList<Integer>();
    for (User user : users) {
      int id = dao.insert(user);
      user.setId(id);
      ids.add(id);
    }
    for (Integer id : ids) {
      assertThat(cacheHandler.get(getUserKey(id)), nullValue());
    }
    dao.getUserList(ids);
    for (int i = 0; i < ids.size(); i++) {
      int id = ids.get(i);
      User user = users.get(i);
      assertThat((User) cacheHandler.get(getUserKey(id)), equalTo(user));
    }
    String name = "ash";
    int r = dao.updateWithInStatement(ids, name);
    assertThat(r, greaterThan(0));
    for (int i = 0; i < ids.size(); i++) {
      int id = ids.get(i);
      assertThat(cacheHandler.get(getUserKey(id)), nullValue());
      users.get(i).setName(name);
    }

    List<User> actual = dao.getUserList(ids);
    assertThat(actual, hasSize(users.size()));
    assertThat(actual, containsInAnyOrder(users.toArray()));
  }

  @Test
  public void testBatchUpdate() {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    UserDao dao = mango.create(UserDao.class);
    List<User> users = createRandomUsers(5);
    List<Integer> ids = new ArrayList<Integer>();
    for (User user : users) {
      int id = dao.insert(user);
      user.setId(id);
      ids.add(id);
    }
    for (Integer id : ids) {
      assertThat(cacheHandler.get(getUserKey(id)), nullValue());
    }
    dao.getUserList(ids);
    for (int i = 0; i < ids.size(); i++) {
      int id = ids.get(i);
      User user = users.get(i);
      assertThat((User) cacheHandler.get(getUserKey(id)), equalTo(user));
    }

    List<User> newUsers = new ArrayList<User>();
    String name = "ash";
    for (int i = 0; i < ids.size(); i++) {
      int id = ids.get(i);
      User newUser = new User();
      newUser.setId(id);
      newUser.setName(name);
      newUsers.add(newUser);
      users.get(i).setName(name);
    }

    int[] r = dao.batchUpdate(newUsers);
    assertThat(r.length, greaterThan(0));
    for (int i = 0; i < ids.size(); i++) {
      int id = ids.get(i);
      assertThat(cacheHandler.get(getUserKey(id)), nullValue());
    }

    List<User> actual = dao.getUserList(ids);
    assertThat(actual, hasSize(users.size()));
    assertThat(actual, containsInAnyOrder(users.toArray()));
  }

  @Test
  public void testBatchUpdateEmpetyList() {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    UserDao dao = mango.create(UserDao.class);
    List<User> users = Lists.newArrayList();
    assertThat(dao.batchUpdate(users).length, equalTo(0));
  }

  @Test
  public void testReturnArrayList() throws Exception {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    List<Msg> msgs = new ArrayList<Msg>();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    MsgDao dao = mango.create(MsgDao.class);
    int uid = 100;
    String key = getMsgKey(uid);

    for (int i = 0; i < 3; i++) {
      Msg msg = createRandomMsg(uid);
      msgs.add(msg);
      msg.setId(dao.insert(msg));
      assertThat(cacheHandler.get(key), nullValue());
      ArrayList<Msg> actual = dao.getArrayMsgs(uid);
      assertThat(actual, hasSize(msgs.size()));
      assertThat(actual, contains(msgs.toArray()));
      @SuppressWarnings("unchecked")
      ArrayList<Msg> cacheActual = (ArrayList<Msg>) cacheHandler.get(key);
      assertThat(cacheActual, hasSize(msgs.size()));
      assertThat(cacheActual, contains(msgs.toArray()));
    }

    ArrayList<Msg> actual = dao.getArrayMsgs(uid);
    assertThat(actual, hasSize(msgs.size()));
    assertThat(actual, contains(msgs.toArray()));

    Msg msg = msgs.get(0);
    msg.setContent("ash");
    dao.update(msg);
    assertThat(cacheHandler.get(key), nullValue());

    actual = dao.getArrayMsgs(uid);
    assertThat(actual, hasSize(msgs.size()));
    assertThat(actual, contains(msgs.toArray()));

    msg = msgs.remove(0);
    dao.delete(msg.getUid(), msg.getId());
    actual = dao.getArrayMsgs(uid);
    assertThat(actual, hasSize(msgs.size()));
    assertThat(actual, contains(msgs.toArray()));
  }

  @Test
  public void testReturnLinkedList() throws Exception {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    List<Msg> msgs = new ArrayList<Msg>();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    MsgDao dao = mango.create(MsgDao.class);
    int uid = 100;
    String key = getMsgKey(uid);

    for (int i = 0; i < 3; i++) {
      Msg msg = createRandomMsg(uid);
      msgs.add(msg);
      msg.setId(dao.insert(msg));
      assertThat(cacheHandler.get(key), nullValue());
      LinkedList<Msg> actual = dao.getLinkedMsgs(uid);
      assertThat(actual, hasSize(msgs.size()));
      assertThat(actual, contains(msgs.toArray()));
      @SuppressWarnings("unchecked")
      LinkedList<Msg> cacheActual = (LinkedList<Msg>) cacheHandler.get(key);
      assertThat(cacheActual, hasSize(msgs.size()));
      assertThat(cacheActual, contains(msgs.toArray()));
    }

    LinkedList<Msg> actual = dao.getLinkedMsgs(uid);
    assertThat(actual, hasSize(msgs.size()));
    assertThat(actual, contains(msgs.toArray()));

    Msg msg = msgs.get(0);
    msg.setContent("ash");
    dao.update(msg);
    assertThat(cacheHandler.get(key), nullValue());

    actual = dao.getLinkedMsgs(uid);
    assertThat(actual, hasSize(msgs.size()));
    assertThat(actual, contains(msgs.toArray()));

    msg = msgs.remove(0);
    dao.delete(msg.getUid(), msg.getId());
    actual = dao.getLinkedMsgs(uid);
    assertThat(actual, hasSize(msgs.size()));
    assertThat(actual, contains(msgs.toArray()));
  }

  @Test
  public void testQueryEmpty() throws Exception {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    UserDao dao = mango.create(UserDao.class);
    boolean old = mango.isCompatibleWithEmptyList();
    mango.setCompatibleWithEmptyList(true);
    assertThat(dao.getUserArray(new ArrayList<Integer>()).length, equalTo(0));
    mango.setCompatibleWithEmptyList(old);
  }

  @Test
  public void testUpdateEmpty() throws Exception {
    LocalCacheHandler cacheHandler = new LocalCacheHandler();
    Mango mango = Mango.newInstance(ds);
    mango.setCacheHandler(cacheHandler);
    UserDao dao = mango.create(UserDao.class);
    boolean old = mango.isCompatibleWithEmptyList();
    mango.setCompatibleWithEmptyList(true);
    assertThat(dao.updateWithInStatement(new ArrayList<Integer>(), "ash"), equalTo(0));
    mango.setCompatibleWithEmptyList(old);
  }

  private String getUserKey(int id) {
    return "user_" + id;
  }

  private String getMsgKey(int uid) {
    return "msg_" + uid;
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

  private Msg createRandomMsg(int uid) {
    String content = Randoms.randomString(20);
    Msg msg = new Msg();
    msg.setUid(uid);
    msg.setContent(content);
    return msg;
  }


  @DB
  @Cache(prefix = "user", expire = Day.class)
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

    @SQL("select id, name, age, gender, money, update_time from user where id in (:1)")
    public Set<User> getUserSet(@CacheBy() List<Integer> ids);

    @SQL("select id, name, age, gender, money, update_time from user where id in (:1)")
    public User[] getUserArray(@CacheBy() List<Integer> ids);

    @SQL("update user set name=:2 where id in (:1)")
    public int updateWithInStatement(@CacheBy List<Integer> ids, String name);

    @SQL("update user set name=:1.name where id=:1.id")
    public int[] batchUpdate(@CacheBy("id") List<User> users);

  }

  @DB
  @Cache(prefix = "msg", expire = Day.class)
  interface MsgDao {

    @ReturnGeneratedId
    @SQL("insert into msg(uid, content) values(:m.uid, :m.content)")
    public int insert(@CacheBy("uid") @Rename("m") Msg msg);

    @SQL("delete from msg where uid=:1 and id=:2")
    public int delete(@CacheBy int uid, int id);

    @SQL("update msg set content=:1.content where id=:1.id and uid=:1.uid")
    public int update(@CacheBy("uid") Msg msg);

    @SQL("select id, uid, content from msg where uid=:1 order by id")
    public List<Msg> getMsgs(@CacheBy int uid);

    @SQL("select id, uid, content from msg where uid=:1 order by id")
    public ArrayList<Msg> getArrayMsgs(@CacheBy int uid);

    @SQL("select id, uid, content from msg where uid=:1 order by id")
    public LinkedList<Msg> getLinkedMsgs(@CacheBy int uid);

  }

}
