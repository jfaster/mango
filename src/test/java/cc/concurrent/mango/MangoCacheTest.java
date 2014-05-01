/*
 * Copyright 2014 mango.concurrent.cc
 *
 * The Netty Project licenses this file to you under the Apache License,
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

import com.google.common.base.Objects;
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
public class MangoCacheTest {


    private final static DataSource ds = Config.getDataSource();
    private final static Mango mango = new Mango(ds);

    @Before
    public void before() throws Exception {
        Connection conn = ds.getConnection();
        Sqls.USER.run(conn);
        Sqls.MSG.run(conn);
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
        String name = getRandomString(20);
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
        String content = getRandomString(20);
        Msg msg = new Msg();
        msg.setUid(uid);
        msg.setContent(content);
        return msg;
    }

    private static String getRandomString(int maxLength) {
        Random r = new Random();
        int length = r.nextInt(maxLength);
        StringBuffer buffer = new StringBuffer("0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
        StringBuffer sb = new StringBuffer();
        int range = buffer.length();
        for (int i = 0; i < length; i ++) {
            sb.append(buffer.charAt(r.nextInt(range)));
        }
        return sb.toString();
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

    public static class User {

        private int id;
        private String name;
        private int age;
        private boolean gender;
        private Long money;
        private Date updateTime;

        public User() {
        }

        public User(String name, int age, boolean gender, long money, Date updateTime) {
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.money = money;
            this.updateTime = updateTime != null ? new Date(updateTime.getTime() / 1000 * 1000) : null; // 精确到秒
        }

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final User other = (User) obj;
            Long thisUpdateTime = this.updateTime != null ? this.updateTime.getTime() : null;
            Long otherUpdateTime = other.updateTime != null ? other.updateTime.getTime() : null;
            return Objects.equal(this.id, other.id)
                    && Objects.equal(this.name, other.name)
                    && Objects.equal(this.age, other.age)
                    && Objects.equal(this.gender, other.gender)
                    && Objects.equal(this.money, other.money)
                    && Objects.equal(thisUpdateTime, otherUpdateTime);
        }

        @Override
        public String toString() {
            Long thisUpdateTime = this.updateTime != null ? this.updateTime.getTime() : null;
            return Objects.toStringHelper(this).add("id", id).add("name", name).add("age", age).
                    add("gender", gender).add("money", money).add("updateTime", thisUpdateTime).toString();
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public boolean isGender() {
            return gender;
        }

        public void setGender(boolean gender) {
            this.gender = gender;
        }

        public Long getMoney() {
            return money;
        }

        public void setMoney(Long money) {
            this.money = money;
        }

        public Date getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Date updateTime) {
            this.updateTime = updateTime;
        }
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

    public static class Msg {
        private int id;
        private int uid;
        private String content;

        @Override
        public boolean equals(Object obj) {
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            final Msg other = (Msg) obj;
            return Objects.equal(this.id, other.id)
                    && Objects.equal(this.uid, other.uid)
                    && Objects.equal(this.content, other.content);
        }

        @Override
        public String toString() {
            return Objects.toStringHelper(this).add("id", id).add("uid", uid).add("content", content).toString();
        }

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public int getUid() {
            return uid;
        }

        public void setUid(int uid) {
            this.uid = uid;
        }

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }

}
