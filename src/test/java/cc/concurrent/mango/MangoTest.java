package cc.concurrent.mango;

import cc.concurrent.mango.annotation.SQL;
import cc.concurrent.mango.logging.InternalLoggerFactory;
import cc.concurrent.mango.logging.Slf4JLoggerFactory;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Scanner;

/**
 * @author ash
 */
public class MangoTest {

    private static DataSource ds;
    private static Connection conn;
    private static UserDao dao;

    @BeforeClass
    public static void beforeClass() throws Exception {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        ds = new DriverManagerDataSource("jdbc:hsqldb:mem:aname", "sa", "");
        conn = ds.getConnection();
        dao = new Mango(ds).create(UserDao.class);

        Statement stat = conn.createStatement();
        String table = fileToString("/user.sql");
        stat.execute(table); // 创建表
        stat.close();
    }

    @Test
    public void testSelect() throws Exception {
        String sql = "insert into user(name, age, gender, money, update_time) values(?, ?, ?, ?, ?)";
        PreparedStatement pstat = conn.prepareStatement(sql);
        pstat.setString(1, "ash");
        pstat.setInt(2, 26);
        pstat.setBoolean(3, true);
        pstat.setLong(4, 10000000L);
        pstat.setTimestamp(5, new Timestamp(System.currentTimeMillis()));
        pstat.executeUpdate();

        System.out.println(dao.selectId(1));

        pstat.close();
    }

    @AfterClass
    public static void afterClass() throws Exception {
        conn.close();
    }

    private static String fileToString(String name) {
        InputStream is = MangoTest.class.getResourceAsStream(name);
        Scanner s = new Scanner(is);
        StringBuffer sb = new StringBuffer();
        while (s.hasNextLine()) {
            sb.append(s.nextLine()).append(System.getProperty("line.separator"));
        }
        return sb.toString();
    }

    public static class User {

        private int id;
        private String name;
        private int age;
        private boolean gender;
        private long money;
        private Date updateTime;

        public User(String name, int age, boolean gender, long money, Date updateTime) {
            this.name = name;
            this.age = age;
            this.gender = gender;
            this.money = money;
            this.updateTime = updateTime;
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

        public long getMoney() {
            return money;
        }

        public void setMoney(long money) {
            this.money = money;
        }

        public Date getUpdateTime() {
            return updateTime;
        }

        public void setUpdateTime(Date updateTime) {
            this.updateTime = updateTime;
        }
    }

    public static interface UserDao {

        @SQL("select id from user where id = :1")
        public Integer selectId(int id);

    }

}
