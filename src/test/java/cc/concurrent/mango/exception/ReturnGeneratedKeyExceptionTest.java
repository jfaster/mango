package cc.concurrent.mango.exception;

import cc.concurrent.mango.*;
import cc.concurrent.mango.model4table.Tables;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author ash
 */
public class ReturnGeneratedKeyExceptionTest {

    private final static DataSource ds = Config.getDataSource();
    private final static Mango mango = new Mango(ds);

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() throws Exception {
        Connection conn = ds.getConnection();
        Tables.PERSON.load(conn);
        conn.close();
    }

    @Test
    public void test() {
        thrown.expect(ReturnGeneratedKeyException.class);
        thrown.expectMessage("please check whether the table has auto increment key");
        PersonDao dao = mango.create(PersonDao.class);
        dao.add(1, "name");
    }

    @DB
    interface PersonDao {
        @ReturnGeneratedId
        @SQL("insert into person(id, name) values(:1, :2)")
        public int add(int id, String name);
    }

}
