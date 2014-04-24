package cc.concurrent.mango;

import org.junit.Before;
import org.junit.Test;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;

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
        conn.close();
    }


    @Test
    public void testSingleKey() throws Exception {
        CacheHandler cacheHandler = new CacheHandlerImpl();
        UserDao dao = mango.create(UserDao.class, cacheHandler);
        User user = createRandomUser();
        int id = dao.insert(user);
        String key = getKey(id);
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
    public void testMultiKeys() throws Exception {
        CacheHandler cacheHandler = new CacheHandlerImpl();
        UserDao dao = mango.create(UserDao.class, cacheHandler);
        List<User> users = createRandomUsers(5);
        List<Integer> ids = new ArrayList<Integer>();
        String name = "ash";
        for (User user : users) {
            user.setName(name);
            int id = dao.insert(user);
            user.setId(id);
            ids.add(id);
        }
        List<User> actual = dao.selectList(ids, name);
        assertThat(actual, hasSize(users.size()));
        assertThat(actual, containsInAnyOrder(users.toArray()));
        for (int i = 0; i < ids.size(); i++) {
            Integer id = ids.get(i);
            User user = users.get(i);
            assertThat((User) cacheHandler.get(getKey(id)), equalTo(user));
        }
        actual = dao.selectList(ids, name);
        assertThat(actual, hasSize(users.size()));
        assertThat(actual, containsInAnyOrder(users.toArray()));
        cacheHandler.delete(getKey(users.get(0).getId()));
        actual = dao.selectList(ids, name);
        assertThat(actual, hasSize(users.size()));
        assertThat(actual, containsInAnyOrder(users.toArray()));
    }

    private String getKey(int id) {
        return "user_" + id;
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

        @SQL("select id, name, age, gender, money, update_time from user where id in (:1) and name=:2")
        public List<User> selectList(@CacheBy() List<Integer> ids, String name);

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
