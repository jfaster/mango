package cc.concurrent.mango;

import cc.concurrent.mango.dao.UserDao;
import cc.concurrent.mango.logging.InternalLoggerFactory;
import cc.concurrent.mango.logging.Slf4JLoggerFactory;
import com.google.common.collect.Lists;
import com.google.common.reflect.Invokable;
import com.google.common.reflect.TypeToken;
import org.junit.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * @author ash
 */
public class MangoTest {

    @Test
    public void testCreate() throws Exception {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        UserDao userDao = new Mango().create(UserDao.class);
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
