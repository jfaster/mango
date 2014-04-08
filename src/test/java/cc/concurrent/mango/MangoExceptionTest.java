package cc.concurrent.mango;

import cc.concurrent.mango.support.ExceptionDao;
import cc.concurrent.mango.support.User;
import cc.concurrent.mango.util.logging.InternalLoggerFactory;
import cc.concurrent.mango.util.logging.Slf4JLoggerFactory;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

import javax.sql.DataSource;

/**
 * 测试异常
 *
 * @author ash
 */
@Ignore
public class MangoExceptionTest {

    private static DataSource ds;
    private static ExceptionDao dao;

    @BeforeClass
    public static void beforeClass() throws Exception {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());

        String driverClassName = Config.getDriverClassName();
        String url = Config.getUrl();
        String username = Config.getUsername();
        String password = Config.getPassword();
        ds = new DriverManagerDataSource(driverClassName, url, username, password);
        dao = new Mango(new SimpleDataSourceFactory(ds), null).create(ExceptionDao.class);
    }


    @Test
    public void testNotReadableParameterException() {
        dao.updateUser(new User());
    }

    @Test
    public void testNotReadablePropertyException() {
        dao.updateUser2(new User());
    }

    @Test
    public void testIncorrectParameterTypeException() {
        dao.updateUser3(new User());
    }

}
