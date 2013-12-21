package cc.concurrent.mango;

import cc.concurrent.mango.logging.InternalLoggerFactory;
import cc.concurrent.mango.logging.Slf4JLoggerFactory;
import cc.concurrent.mango.support.User;
import cc.concurrent.mango.support.UserDao;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.*;
import java.util.*;
import java.util.Date;

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
        ds = new DriverManagerDataSource("jdbc:hsqldb:mem:test", "sa", "");
        dao = new Mango(ds).create(UserDao.class);

        initUsers();
        createTable();
        insertData();
    }

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


/***********************************************************************/

    @Test
    public void testUpdate() throws Exception {
        String name = "ash";
        int age = 11000;
        boolean gender = true;
        long money = 9999999999L;
        Date updateTime = new Date();
        User user = new User(name, age, gender, money, updateTime);
        dao.insertUser(user);
    }




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
