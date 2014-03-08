package cc.concurrent.mango;

import cc.concurrent.mango.support.Man;
import cc.concurrent.mango.support.ManDao;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import cc.concurrent.mango.util.logging.Slf4JLoggerFactory;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.sql.DataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * 测试cache
 *
 * @author ash
 */
public class MangoCacheTest {

    private static ManDao dao;
    private static DataCache dataCache;

    @BeforeClass
    public static void beforeClass() throws Exception {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        String driverClassName = Config.getDriverClassName();
        String url = Config.getUrl();
        String username = Config.getUsername();
        String password = Config.getPassword();
        DriverManagerDataSource ds = new DriverManagerDataSource(driverClassName, url, username, password);
        createTable(ds);
        dataCache = new DataCacheImpl();
        dao = new Mango(ds, dataCache).create(ManDao.class);
    }

    // TODO 便利的debug log
    @Test
    public void testSingleKey() throws Exception {
        Man man = new Man("ash", 26, true, 10086, new Date());
        int id = dao.insert(man);
        String key = getKey(id);
        man.setId(id);
        assertThat(dataCache.get(key), nullValue());
        assertThat(dao.select(id), equalTo(man));
        assertThat((Man) dataCache.get(key), equalTo(man));
        assertThat(dao.select(id), equalTo(man));

        man.setName("lulu");
        dao.update(man);
        assertThat(dataCache.get(key), nullValue());
        assertThat(dao.select(id), equalTo(man));
        assertThat((Man) dataCache.get(key), equalTo(man));

        dao.delete(id);
        assertThat(dataCache.get(key), nullValue());
        assertThat(dao.select(id), nullValue());
    }

    @Test
    public void testMultiKeys() throws Exception {
        int num = 5;
        String name = "ash";
        TreeSet<Man> manSet = Sets.newTreeSet();
        List<Man> mans = Lists.newArrayList();
        List<Integer> ids = Lists.newArrayList();
        for (int i = 0; i < num; i++) {
            Man man = new Man(name, 26 + i, true, 10086, new Date());
            int id = dao.insert(man);
            man.setId(id);
            manSet.add(man);
            mans.add(man);
            ids.add(id);
        }
        assertThat(Sets.newTreeSet(dao.selectList(ids, name)), equalTo(manSet));
        for (int i = 0; i < ids.size(); i++) {
            Integer id = ids.get(i);
            Man man = mans.get(i);
            assertThat((Man) dataCache.get(getKey(id)), equalTo(man));
        }
        assertThat(Sets.newTreeSet(dao.selectList(ids, name)), equalTo(manSet));
    }

    /**
     * 创建表
     * @throws java.sql.SQLException
     */
    private static void createTable(DataSource ds) throws SQLException {
        Connection conn = ds.getConnection();
        Statement stat = conn.createStatement();
        String sqls = fileToString("/" + Config.getDir() + "/man.sql");
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

    private String getKey(int id) {
        return "man_" + id;
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
