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

import com.google.common.primitives.Ints;
import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

/**
 * 测试db
 *
 * @author ash
 */
public class MangoTest {

    private final static DataSource ds = Config.getDataSource();
    private final static Mango mango = new Mango(ds);
    private final static UserDao dao = mango.create(UserDao.class);

    @Before
    public void before() throws Exception {
        Connection conn = ds.getConnection();
        Sqls.USER.run(conn);
        conn.close();
    }

/*********************************测试查询开始*************************************/

    @Test
    public void testQueryInteger() throws Exception {
        User user = createRandomUser();
        int id = dao.insertUser(user);
        assertThat(dao.selectInteger(id), equalTo(id));
    }

    @Test
    public void testQueryInt() throws Exception {
        User user = createRandomUser();
        int id = dao.insertUser(user);
        assertThat(dao.selectInt(id), equalTo(id));
    }

    @Test
    public void testQueryString() throws Exception {
        User user = createRandomUser();
        int id = dao.insertUser(user);
        assertThat(dao.selectString(id), equalTo(user.getName()));
    }

    @Test
    public void testQueryBooleanObj() throws Exception {
        User user = createRandomUser();
        int id = dao.insertUser(user);
        assertThat(dao.selectBooleanObj(id), equalTo(user.isGender()));
    }

    @Test
    public void testQueryBoolean() throws Exception {
        User user = createRandomUser();
        int id = dao.insertUser(user);
        assertThat(dao.selectBoolean(id), equalTo(user.isGender()));
    }

    @Test
    public void testQueryLongObj() throws Exception {
        User user = createRandomUser();
        int id = dao.insertUser(user);
        assertThat(dao.selectLongObj(id), equalTo(user.getMoney()));
    }

    @Test
    public void testQueryLong() throws Exception {
        User user = createRandomUser();
        int id = dao.insertUser(user);
        assertThat(dao.selectLong(id), equalTo(user.getMoney()));
    }

    @Test
    public void testQueryDate() throws Exception {
        User user = createRandomUser();
        int id = dao.insertUser(user);
        assertThat(dao.selectDate(id).getTime(), equalTo(user.getUpdateTime().getTime()));
    }

    @Test
    public void testQueryUser() throws Exception {
        User user = createRandomUser();
        int id = dao.insertUser(user);
        user.setId(id);
        assertThat(dao.selectUser(id), equalTo(user));
    }

    @Test
    public void testQueryUserList() throws Exception {
        List<User> users = createRandomUsers(10);
        for (User user : users) {
            int id = dao.insertUser(user);
            user.setId(id);
        }
        List<User> actual = dao.selectUserList();
        assertThat(actual, hasSize(users.size()));
        assertThat(actual, contains(users.toArray()));
    }

    @Test
    public void testQueryUserSet() throws Exception {
        List<User> users = createRandomUsers(10);
        for (User user : users) {
            int id = dao.insertUser(user);
            user.setId(id);
        }
        Set<User> actual = dao.selectUserSet();
        assertThat(actual, hasSize(users.size()));
        assertThat(actual, containsInAnyOrder(users.toArray()));
    }

    @Test
    public void testQueryUserArray() throws Exception {
        List<User> users = createRandomUsers(10);
        for (User user : users) {
            int id = dao.insertUser(user);
            user.setId(id);
        }
        List<User> actual = Arrays.asList(dao.selectUserArray());
        assertThat(actual, hasSize(users.size()));
        assertThat(actual, contains(users.toArray()));
    }

    @Test
    public void testQueryIntegerArray() throws Exception {
        List<Integer> ids = new ArrayList<Integer>();
        for (User user : createRandomUsers(10)) {
            int id = dao.insertUser(user);
            ids.add(id);
        }
        List<Integer> actual = Arrays.asList(dao.selectIntegerArray());
        assertThat(actual, hasSize(ids.size()));
        assertThat(actual, contains(ids.toArray()));
    }

    @Test
    public void testQueryIntArray() throws Exception {
        List<Integer> ids = new ArrayList<Integer>();
        for (User user : createRandomUsers(10)) {
            int id = dao.insertUser(user);
            ids.add(id);
        }
        List<Integer> actual = Ints.asList(dao.selectIntArray());
        assertThat(actual, hasSize(ids.size()));
        assertThat(actual, contains(ids.toArray()));
    }

    @Test
    public void testQeuryInList() throws Exception {
        List<User> users = createRandomUsers(10);
        List<Integer> ids = new ArrayList<Integer>();
        for (User user : users) {
            int id = dao.insertUser(user);
            ids.add(id);
            user.setId(id);
        }
        List<User> actual = dao.selectUserInList(ids);
        assertThat(actual, hasSize(users.size()));
        assertThat(actual, containsInAnyOrder(users.toArray()));
    }

    @Test
    public void testQueryInSet() throws Exception {
        List<User> users = createRandomUsers(10);
        Set<Integer> ids = new HashSet<Integer>();
        for (User user : users) {
            int id = dao.insertUser(user);
            ids.add(id);
            user.setId(id);
        }
        List<User> actual = dao.selectUserInSet(ids);
        assertThat(actual, hasSize(users.size()));
        assertThat(actual, containsInAnyOrder(users.toArray()));
    }

    @Test
    public void testQueryInIntegerArray() throws Exception {
        List<User> users = createRandomUsers(10);
        Integer[] ids = new Integer[users.size()];
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            int id = dao.insertUser(user);
            ids[i] = id;
            user.setId(id);
        }
        List<User> actual = dao.selectUserInIntegerArray(ids);
        assertThat(actual, hasSize(users.size()));
        assertThat(actual, containsInAnyOrder(users.toArray()));
    }

    @Test
    public void testQueryInIntArray() throws Exception {
        List<User> users = createRandomUsers(10);
        int[] ids = new int[users.size()];
        for (int i = 0; i < users.size(); i++) {
            User user = users.get(i);
            int id = dao.insertUser(user);
            ids[i] = id;
            user.setId(id);
        }
        List<User> actual = dao.selectUserInIntArray(ids);
        assertThat(actual, hasSize(users.size()));
        assertThat(actual, containsInAnyOrder(users.toArray()));
    }
//
//    @Test(expected = NullPointerException.class)
//    public void testQueryIntegerNull() throws Exception {
//        int id = Integer.MAX_VALUE;
//        dao.selectInt(id);
//    }
//


/********************************测试更新开始***************************************/

    @Test
    public void testUpdate() throws Exception {
        User user = createRandomUser();
        int id = dao.insertUser(user);
        user.setId(id);
        User user2 = dao.selectUser(id);
        assertThat(user2, equalTo(user));
        User user3 = createRandomUser();
        user3.setId(id);
        int r = dao.updateUser(user3);
        assertThat(r, equalTo(1));
        User user4 = dao.selectUser(id);
        assertThat(user4, equalTo(user3));
        r = dao.deleteUser(id);
        assertThat(r, equalTo(1));
        User user5 = dao.selectUser(id);
        assertThat(user5, equalTo(null));
    }

    @Test
    public void testUpdateSelectNull() throws Exception {
        User user = createRandomUser();
        user.setName(null);
        user.setUpdateTime(null);
        int id = dao.insertUser(user);
        user.setId(id);
        User user2 = dao.selectUser(id);
        assertThat(user2, equalTo(user));
        String r = dao.selectString(id);
        assertThat(r, equalTo(null));
        user.setMoney(null);
        int r2 = dao.updateUser(user);
        assertThat(r2, equalTo(1));
        Long r3 = dao.selectLongObj(id);
        assertThat(r3, equalTo(null));
    }

/********************************测试批量更新开始***************************************/

    @Test
    public void testBatchUpdateList() throws Exception {
        List<User> users = createRandomUsers(20);
        int age = 10086;
        for (User user : users) {
            user.setAge(age);
        }
        int[] r = dao.batchInsertUserList(users);
        assertThat(r.length, equalTo(users.size()));
        assertThat(Ints.asList(r), everyItem(equalTo(1)));
        List<User> actual = dao.selectUserByAge(age);
        assertThat(actual, hasSize(users.size()));
        for (int i = 0; i < users.size(); i++) {
            actual.get(i).setId(0);
        }
        assertThat(actual, contains(users.toArray()));
    }

    @Test
    public void testBatchUpdateSet() throws Exception {
        Set<User> users = new HashSet<User>(createRandomUsers(20));
        int age = 10086;
        for (User user : users) {
            user.setAge(age);
        }
        int[] r = dao.batchInsertUserSet(users);
        assertThat(r.length, equalTo(users.size()));
        assertThat(Ints.asList(r), everyItem(equalTo(1)));
        List<User> actual = dao.selectUserByAge(age);
        assertThat(actual, hasSize(users.size()));
        for (int i = 0; i < users.size(); i++) {
            actual.get(i).setId(0);
        }
        assertThat(actual, containsInAnyOrder(users.toArray()));
    }

    @Test
    public void testBatchUpdateArray() throws Exception {
        User[] users = createRandomUsers(20).toArray(new User[0]);
        int age = 10086;
        for (User user : users) {
            user.setAge(age);
        }
        int[] r = dao.batchInsertUserArray(users);
        assertThat(r.length, equalTo(users.length));
        assertThat(Ints.asList(r), everyItem(equalTo(1)));
        List<User> actual = dao.selectUserByAge(age);
        assertThat(actual, hasSize(users.length));
        for (int i = 0; i < users.length; i++) {
            actual.get(i).setId(0);
        }
        assertThat(actual, contains(users));
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

    @DB(table = "user")
    static interface UserDao {

        @SQL("select id from user where id = :1")
        public Integer selectInteger(int id);

        @SQL("select id from user where id = :1")
        public int selectInt(int id);

        @SQL("select name from user where id = :1")
        public String selectString(int id);

        @SQL("select gender from user where id = :1")
        public Boolean selectBooleanObj(int id);

        @SQL("select gender from user where id = :1")
        public boolean selectBoolean(int id);

        @SQL("select money from user where id = :1")
        public Long selectLongObj(int id);

        @SQL("select money from user where id = :1")
        public long selectLong(int id);

        @SQL("select update_time from user where id = :1")
        public Date selectDate(int id);

        @SQL("select id, name, age, gender, money, update_time from user where id = :1")
        public User selectUser(int id);

        @SQL("select id, name, age, gender, money, update_time from user order by id")
        public List<User> selectUserList();

        @SQL("select id, name, age, gender, money, update_time from user order by id")
        public Set<User> selectUserSet();

        @SQL("select id, name, age, gender, money, update_time from user order by id")
        public User[] selectUserArray();

        @SQL("select id from user order by id")
        public Integer[] selectIntegerArray();

        @SQL("select id from user order by id")
        public int[] selectIntArray();

        @SQL("select id, name, age, gender, money, update_time from user where id in (:1)")
        public List<User> selectUserInList(List<Integer> ids);

        @SQL("select id, name, age, gender, money, update_time from user where id in (:1)")
        public List<User> selectUserInSet(Set<Integer> ids);

        @SQL("select id, name, age, gender, money, update_time from user where id in (:1)")
        public List<User> selectUserInIntegerArray(Integer[] ids);

        @SQL("select id, name, age, gender, money, update_time from user where id in (:1)")
        public List<User> selectUserInIntArray(int[] ids);

        @SQL("select id, name, age, gender, money, update_time from user where age=:1 order by id")
        public List<User> selectUserByAge(int age);

        @SQL("select max(id) from user")
        public int selectMaxInt();

        /***********************************************************************/

        @ReturnGeneratedId
        @SQL("insert into user(name, age, gender, money, update_time) values(:1.name, :1.age, :1.gender, :1.money, :1.updateTime)")
        public int insertUser(User user);

        @SQL("update ${:table} set name=:u.name, age=:u.age, gender=:u.gender, money=:u.money, update_time=:u.updateTime where id=:u.id")
        public int updateUser(@Rename("u") User user);

        @SQL("delete from user where id=:1")
        public int deleteUser(int id);

        @SQL("delete from user where age=:1")
        public int deleteUserByAge(int age);

        /***********************************************************************/

        @SQL("insert into user(name, age, gender, money, update_time) values(:1.name, :1.age, :1.gender, :1.money, :1.updateTime)")
        public int[] batchInsertUserList(List<User> userList);

        @SQL("insert into user(name, age, gender, money, update_time) values(:1.name, :1.age, :1.gender, :1.money, :1.updateTime)")
        public int[] batchInsertUserSet(Set<User> userSet);

        @SQL("insert into user(name, age, gender, money, update_time) values(:1.name, :1.age, :1.gender, :1.money, :1.updateTime)")
        public int[] batchInsertUserArray(User[] users);

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
            return com.google.common.base.Objects.equal(this.id, other.id)
                    && com.google.common.base.Objects.equal(this.name, other.name)
                    && com.google.common.base.Objects.equal(this.age, other.age)
                    && com.google.common.base.Objects.equal(this.gender, other.gender)
                    && com.google.common.base.Objects.equal(this.money, other.money)
                    && com.google.common.base.Objects.equal(thisUpdateTime, otherUpdateTime);
        }

        @Override
        public String toString() {
            Long thisUpdateTime = this.updateTime != null ? this.updateTime.getTime() : null;
            return com.google.common.base.Objects.toStringHelper(this).add("id", id).add("name", name).add("age", age).
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

}
