package org.jfaster.mango.jdbc.exception;

import org.jfaster.mango.jdbc.GeneratedKeyHolder;
import org.jfaster.mango.jdbc.JdbcTemplate;
import org.jfaster.mango.support.Config;
import org.jfaster.mango.support.Table;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import javax.sql.DataSource;
import java.sql.Connection;

/**
 * @author ash
 */
public class DataRetrievalFailureExceptionTest {

    private final static DataSource ds = Config.getDataSource();

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Before
    public void before() throws Exception {
        Connection conn = ds.getConnection();
        Table.PERSON.load(conn);
        conn.close();
    }

    @Test
    public void test() {
        thrown.expect(DataRetrievalFailureException.class);
        thrown.expectMessage("Unable to retrieve the generated key. Check that the table has an identity column enabled.");
        JdbcTemplate t = new JdbcTemplate();
        t.update(ds, "insert into person(id, name) values(?, ?)", new Object[]{1, "ash"}, new GeneratedKeyHolder(int.class));
    }

}
