package cc.concurrent.mango.exception;

import cc.concurrent.mango.Config;
import cc.concurrent.mango.DB;
import cc.concurrent.mango.Mango;
import cc.concurrent.mango.SQL;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试{@link IncorrectParameterCountException}
 *
 * @author ash
 */
public class IncorrectParameterCountExceptionTest {

    private final static Mango mango = new Mango(Config.getDataSource());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() {
        thrown.expect(IncorrectParameterCountException.class);
        thrown.expectMessage("batch update expected one and only one parameter but 2");
        Dao dao = mango.create(Dao.class);
        dao.batchAdd(new ArrayList<Integer>(), 1);
    }

    @DB
    static interface Dao {
        @SQL("insert into ...")
        public int[] batchAdd(List<Integer> list, int a);
    }


}
