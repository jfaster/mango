package cc.concurrent.mango;

import cc.concurrent.mango.dao.StudentDao;
import cc.concurrent.mango.logging.InternalLoggerFactory;
import cc.concurrent.mango.logging.Slf4JLoggerFactory;
import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.io.Files;
import org.junit.Test;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import javax.sql.DataSource;
import java.io.File;
import java.sql.Connection;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;

/**
 * @author ash
 */
public class MangoTest {

    @Test
    public void testCreate() throws Exception {
        InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory());
        DataSource ds = new DriverManagerDataSource("jdbc:hsqldb:mem:aname", "sa", "");
        Connection conn = ds.getConnection();
        Statement stat = conn.createStatement();
        String create = Files.toString(new File("C:\\Users\\Administrator\\IdeaProjects\\mango\\src\\test\\hsqldb\\student.sql"), Charsets.UTF_8);
        stat.execute(create);
        stat.executeUpdate("insert into student(id, name) values(1, 'ash')");
        stat.executeUpdate("insert into student(id, name) values(2, 'lucy')");

        Mango mango = new Mango(ds);
        System.out.println(mango.create(StudentDao.class).getNameById(1));
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
