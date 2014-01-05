package cc.concurrent.mango;

import cc.concurrent.mango.support.CacheUser;
import cc.concurrent.mango.support.CacheUserDao;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import cc.concurrent.mango.util.logging.Slf4JLoggerFactory;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

/**
 * @author ash
 */
public class MangoCacheTest {

    private static CacheUserDao dao;

    @BeforeClass
    public static void beforeClass() throws Exception {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        DriverManagerDataSource ds = new DriverManagerDataSource("jdbc:hsqldb:mem:test", "sa", "");
        createTable(ds);
        dao = new Mango(ds, new DataCacheImpl()).create(CacheUserDao.class);
    }

    @Test
    public void test() throws Exception {
        CacheUser user = new CacheUser();
        user.setAge(1);
        int id = dao.insert(user);
        user.setId(id);
        user.setAge(2);
        dao.updateUser(user);
    }

    /**
     * 创建表
     * @throws java.sql.SQLException
     */
    private static void createTable(DataSource ds) throws SQLException {
        Connection conn = ds.getConnection();
        Statement stat = conn.createStatement();
        String table = fileToString("/cache_user.sql");
        stat.execute(table);
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

    private static class DataCacheImpl implements DataCache {

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
        public void set(String key, Object value) {
            cache.put(key, value);
        }

        @Override
        public void delete(Set<String> keys) {
            for (String key : keys) {
                cache.remove(key);
            }
        }
    }

}
