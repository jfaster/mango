package cc.concurrent.mango;

import cc.concurrent.mango.dao.UserDao;
import org.junit.Test;

/**
 * @author ash
 */
public class MangoTest {

    @Test
    public void testCreate() throws Exception {
        UserDao userDao = new Mango().create(UserDao.class);
        userDao.insert(1, "abc");
    }

}
