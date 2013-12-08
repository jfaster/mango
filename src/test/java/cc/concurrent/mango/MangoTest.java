package cc.concurrent.mango;

import cc.concurrent.mango.dao.UserDao;
import cc.concurrent.mango.logging.InternalLoggerFactory;
import cc.concurrent.mango.logging.Slf4JLoggerFactory;
import com.google.common.collect.Lists;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.List;

/**
 * @author ash
 */
public class MangoTest {

    @Test
    public void testCreate() throws Exception {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        DataSource ds = new DriverManagerDataSource();
        UserDao userDao = new Mango(ds).create(UserDao.class);
        userDao.batchInsert(Lists.newArrayList(2, 3));
        userDao.insert(1, "abc");
    }

    @Test
    public void testBatch() throws Exception {
    }

    @Test
    public void test() throws Exception {
        List<Integer> list = Lists.newArrayList(1, 2);
        System.out.println(Collection.class.isAssignableFrom(list.getClass()));
    }


}
