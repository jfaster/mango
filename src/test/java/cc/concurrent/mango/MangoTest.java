package cc.concurrent.mango;

import cc.concurrent.mango.support.DatabaseConfig;
import cc.concurrent.mango.support.User;
import cc.concurrent.mango.support.UserDao;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import cc.concurrent.mango.util.logging.Slf4JLoggerFactory;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class MangoTest {

    private static DataSource ds;
    private static UserDao dao;
    private static User[] users = new User[5];

    @BeforeClass
    public static void beforeClass() throws Exception {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

        String driverClassName = DatabaseConfig.getDriverClassName();
        String url = DatabaseConfig.getUrl();
        String username = DatabaseConfig.getUsername();
        String password = DatabaseConfig.getPassword();
        ds = new DriverManagerDataSource(driverClassName, url, username, password);

        dao = new Mango(ds, null).create(UserDao.class);

        initUsers();
        createTable();
        insertData();
    }

/*********************************测试查询开始*************************************/

    @Test
    public void testQueryInteger() throws Exception {
        int id = 0;
        assertThat(dao.selectInteger(id), equalTo(users[id].getId()));
    }

    @Test
    public void testQueryInt() throws Exception {
        int id = 0;
        assertThat(dao.selectInt(id), equalTo(users[id].getId()));
    }

    @Test
    public void testQueryString() throws Exception {
        int id = 0;
        assertThat(dao.selectString(id), equalTo(users[id].getName()));
    }

    @Test
    public void testQueryBooleanObj() throws Exception {
        int id = 0;
        assertThat(dao.selectBooleanObj(id), equalTo(users[id].isGender()));
    }

    @Test
    public void testQueryBoolean() throws Exception {
        int id = 0;
        assertThat(dao.selectBoolean(id), equalTo(users[id].isGender()));
    }

    @Test
    public void testQueryLongObj() throws Exception {
        int id = 0;
        assertThat(dao.selectLongObj(id), equalTo(users[id].getMoney()));
    }

    @Test
    public void testQueryLong() throws Exception {
        int id = 0;
        assertThat(dao.selectLong(id), equalTo(users[id].getMoney()));
    }

    @Test
    public void testQueryDate() throws Exception {
        int id = 0;
        assertThat(dao.selectDate(id).getTime(), equalTo(users[id].getUpdateTime().getTime()));
    }

    @Test
    public void testQueryUser() throws Exception {
        int id = 0;
        assertThat(dao.selectUser(id), equalTo(users[id]));
    }

    @Test
    public void testQueryUserList() throws Exception {
        List<User> userList = dao.selectUserList();
        assertThat(userList.size(), equalTo(users.length));
        for (int i = 0; i < userList.size(); i++) {
            assertThat(userList.get(i), equalTo(users[i]));
        }
    }

    @Test
    public void testQueryUserSet() throws Exception {
        Set<User> userSet = dao.selectUserSet();
        assertThat(userSet.size(), equalTo(users.length));
        for (int i = 0; i < users.length; i++) {
            userSet.remove(users[i]);
        }
        assertThat(userSet.size(), equalTo(0));
    }

    @Test
    public void testQueryUserArray() throws Exception {
        User[] userArray = dao.selectUserArray();
        assertThat(userArray.length, equalTo(users.length));
        for (int i = 0; i < userArray.length; i++) {
            assertThat(userArray[i], equalTo(users[i]));
        }
    }

    @Test
    public void testQueryIntegerArray() throws Exception {
        Integer[] idArray = dao.selectIntegerArray();
        assertThat(idArray.length, equalTo(users.length));
        for (int i = 0; i < idArray.length; i++) {
            assertThat(idArray[i], equalTo(users[i].getId()));
        }
    }

    @Test
    public void testQueryIntArray() throws Exception {
        int[] idArray = dao.selectIntArray();
        assertThat(idArray.length, equalTo(users.length));
        for (int i = 0; i < idArray.length; i++) {
            assertThat(idArray[i], equalTo(users[i].getId()));
        }
    }

    @Test
    public void testQeuryInList() throws Exception {
        List<Integer> ids = Lists.newArrayList();
        for (User user : users) {
            ids.add(user.getId());
        }
        List<User> userList = dao.selectUserInList(ids);
        assertThat(userList.size(), equalTo(users.length));
        for (int i = 0; i < userList.size(); i++) {
            assertThat(userList.get(i), equalTo(users[i]));
        }
    }

    @Test
    public void testQueryInSet() throws Exception {
        Set<Integer> ids = Sets.newHashSet();
        for (User user : users) {
            ids.add(user.getId());
        }
        List<User> userList = dao.selectUserInSet(ids);
        assertThat(userList.size(), equalTo(users.length));
        for (int i = 0; i < users.length; i++) {
            userList.remove(users[i]);
        }
        assertThat(userList.size(), equalTo(0));
    }

    @Test
    public void testQueryInIntegerArray() throws Exception {
        Integer[] ids = new Integer[users.length];
        for (int i = 0; i < users.length; i++) {
            ids[i] = users[i].getId();
        }
        List<User> userList = dao.selectUserInIntegerArray(ids);
        assertThat(userList.size(), equalTo(users.length));
        for (int i = 0; i < userList.size(); i++) {
            assertThat(userList.get(i), equalTo(users[i]));
        }
    }

    @Test
    public void testQueryInIntArray() throws Exception {
        int[] ids = new int[users.length];
        for (int i = 0; i < users.length; i++) {
            ids[i] = users[i].getId();
        }
        List<User> userList = dao.selectUserInIntArray(ids);
        assertThat(userList.size(), equalTo(users.length));
        for (int i = 0; i < userList.size(); i++) {
            assertThat(userList.get(i), equalTo(users[i]));
        }
    }

    @Test(expected = NullPointerException.class)
    public void testQueryIntegerNull() throws Exception {
        int id = Integer.MAX_VALUE;
        dao.selectInt(id);
    }


/********************************测试更新开始***************************************/

    @Test
    public void testUpdate() throws Exception {
        User user = new User("ash", 11000, true, 9999999999L, new Date());
        int id = dao.insertUser(user);
        user.setId(id);
        User user2 = dao.selectUser(id);
        assertThat(user2, equalTo(user));
        User user3 = new User("lucy", 11001, false, 1000000000L, new Date(System.currentTimeMillis() + 100000L));
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
        User user = new User(null, 11000, true, 9999999999L, null);
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
        List<User> userList = Lists.newArrayList();
        int age = 10086;
        for (int i = 0; i < 5; i++) {
            String name = "batch" + i;
            boolean gender = (i % 2 == 0 ? true : false);
            long money = System.currentTimeMillis();
            Date updateTime = new Date();
            User user = new User(name, age, gender, money, updateTime);
            userList.add(user);
        }
        int[] r = dao.batchInsertUserList(userList);
        assertThat(r.length, equalTo(userList.size()));
        for (int i = 0; i < userList.size(); i++) {
            assertThat(r[i], equalTo(1));
        }
        List<User> userList2 = dao.selectUserByAge(age);
        assertThat(userList2.size(), equalTo(userList.size()));
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            User user2 = userList2.get(i);
            user.setId(user2.getId());
            assertThat(user2, equalTo(user));
        }
        dao.deleteUserByAge(age);
    }

    @Test
    public void testBatchUpdateSet() throws Exception {
        Set<User> userSet = Sets.newHashSet();
        int age = 10086;
        for (int i = 0; i < 5; i++) {
            String name = "batch" + i;
            boolean gender = (i % 2 == 0 ? true : false);
            long money = System.currentTimeMillis();
            Date updateTime = new Date();
            User user = new User(name, age, gender, money, updateTime);
            userSet.add(user);
        }
        int[] r = dao.batchInsertUserSet(userSet);
        assertThat(r.length, equalTo(userSet.size()));
        for (int i = 0; i < userSet.size(); i++) {
            assertThat(r[i], equalTo(1));
        }
        List<User> userList2 = dao.selectUserByAge(age);
        assertThat(userList2.size(), equalTo(userSet.size()));
        dao.deleteUserByAge(age);
    }

    @Test
    public void testBatchUpdateArray() throws Exception {
        User[] users = new User[5];
        int age = 10086;
        for (int i = 0; i < users.length; i++) {
            String name = "batch" + i;
            boolean gender = (i % 2 == 0 ? true : false);
            long money = System.currentTimeMillis();
            Date updateTime = new Date();
            User user = new User(name, age, gender, money, updateTime);
            users[i] = user;
        }
        int[] r = dao.batchInsertUserArray(users);
        assertThat(r.length, equalTo(users.length));
        for (int i = 0; i < users.length; i++) {
            assertThat(r[i], equalTo(1));
        }
        List<User> userList2 = dao.selectUserByAge(age);
        assertThat(userList2.size(), equalTo(users.length));
        for (int i = 0; i < users.length; i++) {
            User user = users[i];
            User user2 = userList2.get(i);
            user.setId(user2.getId());
            assertThat(user2, equalTo(user));
        }
        dao.deleteUserByAge(age);
    }


/********************************初始化数据***************************************/

    /**
     * 从文本文件中获得建表语句
     * @param name
     * @return
     */
    private static String fileToString(String name) {
        InputStream is = MangoTest.class.getResourceAsStream(name);
        Scanner s = new Scanner(is);
        StringBuffer sb = new StringBuffer();
        while (s.hasNextLine()) {
            sb.append(s.nextLine()).append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    /**
     * 初始化users
     */
    private static void initUsers() {
        for (int i = 0; i < users.length; i++) {
            String name = "name" + i;
            int age = 26;
            boolean gender = (i % 2 == 0 ? true : false);
            long money = System.currentTimeMillis();
            Date updateTime = new Date();
            users[i] = new User(name, age, gender, money, updateTime);
            users[i].setId(i);
        }
    }

    /**
     * 创建表
     * @throws SQLException
     */
    private static void createTable() throws SQLException {
        Connection conn = ds.getConnection();
        Statement stat = conn.createStatement();
        String table = fileToString("/user.sql");
        stat.execute(table);
        stat.close();
        conn.close();
    }

    /**
     * 插入数据
     */
    private static void insertData() throws SQLException {
        String sql = "insert into user(name, age, gender, money, update_time) values(?, ?, ?, ?, ?)";
        Connection conn = ds.getConnection();
        PreparedStatement pstat = conn.prepareStatement(sql);
        for (User user : users) {
            pstat.setString(1, user.getName());
            pstat.setInt(2, user.getAge());
            pstat.setBoolean(3, user.isGender());
            pstat.setLong(4, user.getMoney());
            pstat.setTimestamp(5, new Timestamp(user.getUpdateTime().getTime()));
            pstat.addBatch();
        }
        pstat.executeBatch();
        pstat.close();
        conn.close();
    }

}
