/*
 * Copyright 2014 mango.concurrent.cc
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

package cc.concurrent.mango;

import cc.concurrent.mango.support.Randoms;
import cc.concurrent.mango.support.Table;
import cc.concurrent.mango.support.model4table.Msg;
import cc.concurrent.mango.support.model4table.User;
import cc.concurrent.mango.support.Config;
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


    private final static DataSource ds = Config.getDataSource();
    private final static Mango mango = new Mango(ds);

    @Before
    public void before() throws Exception {
        Connection conn = ds.getConnection();
        Table.USER.load(conn);
        Table.MSG.load(conn);
        conn.close();
    }


    @Test
    public void testSingleKey() throws Exception {
        CacheHandler cacheHandler = new CacheHandlerImpl();
        UserDao dao = mango.create(UserDao.class, cacheHandler);
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
        CacheHandler cacheHandler = new CacheHandlerImpl();
        UserDao dao = mango.create(UserDao.class, cacheHandler);
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
        CacheHandler cacheHandler = new CacheHandlerImpl();
        UserDao dao = mango.create(UserDao.class, cacheHandler);
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
        CacheHandler cacheHandler = new CacheHandlerImpl();
        UserDao dao = mango.create(UserDao.class, cacheHandler);
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
        CacheHandler cacheHandler = new CacheHandlerImpl();
        List<Msg> msgs = new ArrayList<Msg>();
        MsgDao dao = mango.create(MsgDao.class, cacheHandler);
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

    private static class CacheHandlerImpl implements CacheHandler {

        private Map<String, Object> cache = new HashMap<String, Object>();

        @Override
        public Object get(String key) {
            return cache.get(key);
        }

        @Override
        public Map<String, Object> getBulk(Set<String> keys) {
            Map<String, Object> map = new HashMap<String, Object>();
            for (String key : keys) {
                map.put(key, cache.get(key));
            }
            return map;
        }

        @Override
        public void set(String key, Object value, int expires) {
            cache.put(key, value);
        }

        @Override
        public void delete(Set<String> keys) {
            for (String key : keys) {
                delete(key);
            }
        }

        @Override
        public void delete(String key) {
            cache.remove(key);
        }
    }


    @DB
    @Cache(prefix = "user_", expire = Day.class)
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


        @SQL("select id, name, age, gender, money, update_time from user where age=:1")
        public List<User> getUserListByAge(@CacheBy int age);

    }

    @DB
    @Cache(prefix = "msg_", expire = Day.class)
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

    }

}
