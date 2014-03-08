package cc.concurrent.mango;

import cc.concurrent.mango.support.ByteInfo;
import cc.concurrent.mango.support.ByteInfoDao;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import cc.concurrent.mango.util.logging.Slf4JLoggerFactory;
import com.google.common.base.Splitter;
import org.junit.BeforeClass;
import org.junit.Test;
import sun.security.util.*;
import sun.security.util.Cache;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Arrays;
import java.util.Scanner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * 测试byte[]
 *
 * @author ash
 */
public class MangoByteTest {

    private static ByteInfoDao dao;

    @BeforeClass
    public static void beforeClass() throws Exception {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        String driverClassName = Config.getDriverClassName();
        String url = Config.getUrl();
        String username = Config.getUsername();
        String password = Config.getPassword();
        DriverManagerDataSource ds = new DriverManagerDataSource(driverClassName, url, username, password);
        createTable(ds);
        dao = new Mango(ds, null).create(ByteInfoDao.class);
    }


    @Test
    public void testByteInfo() {
        byte[] arrayByte = new byte[]{1, 2, 3};
        byte singleByte = 10;
        dao.insert(arrayByte, singleByte);
        int id = dao.insert(arrayByte, singleByte);
        assertThat(Arrays.toString(dao.getArrayByte(id)), equalTo(Arrays.toString(arrayByte)));
        for (Byte b : dao.getByteSingles(singleByte)) {
            assertThat(b, equalTo(singleByte));
        }
    }

    /**
     * 创建表
     * @throws java.sql.SQLException
     */
    private static void createTable(DataSource ds) throws SQLException {
        Connection conn = ds.getConnection();
        Statement stat = conn.createStatement();
        String sqls = fileToString("/" + Config.getDir() + "/byte_info.sql");
        for (String sql : Splitter.on("####").trimResults().split(sqls)) {
            stat.execute(sql);
        }
        stat.close();
        conn.close();
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

}
