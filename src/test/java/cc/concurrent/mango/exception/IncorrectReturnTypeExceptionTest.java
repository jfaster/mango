package cc.concurrent.mango.exception;

import cc.concurrent.mango.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试${@link IncorrectReturnTypeException}
 *
 * @author ash
 */
public class IncorrectReturnTypeExceptionTest {

    private final static Mango mango = new Mango(Config.getDataSource());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() {
        thrown.expect(IncorrectReturnTypeException.class);
        thrown.expectMessage("if sql don't start with select, " +
                "update return type expected int, " +
                "batch update return type expected int[], " +
                "but void");
        Dao dao = mango.create(Dao.class);
        dao.update();
    }

    @Test
    public void test2() {
        thrown.expect(IncorrectReturnTypeException.class);
        thrown.expectMessage("if sql has in clause, return type " +
                "expected array or implementations of java.util.List or implementations of java.util.Set " +
                "but int");
        Dao dao = mango.create(Dao.class);
        dao.gets(new ArrayList<Integer>());
    }

    @DB
    static interface Dao {
        @SQL("update ...")
        public void update();

        @SQL("select * from table where ids in (:1)")
        public int gets(List<Integer> ids);
    }

}
