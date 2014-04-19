package cc.concurrent.mango.exception;

import cc.concurrent.mango.*;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

/**
 * 测试{@link IncorrectParameterTypeException}
 *
 * @author ash
 */
public class IncorrectParameterTypeExceptionTest {


    private final static Mango mango = new Mango(Config.getDataSource());

    @Rule
    public ExpectedException thrown = ExpectedException.none();

    @Test
    public void test() {
        thrown.expect(IncorrectParameterTypeException.class);
        thrown.expectMessage("invalid type of :1, " +
                "expected int or java.lang.Integer or java.lang.String " +
                "but class java.lang.Object");
        Dao dao = mango.create(Dao.class);
        dao.add(new Object());
    }

    @Test
    public void test2() {
        thrown.expect(IncorrectParameterTypeException.class);
        thrown.expectMessage("invalid type of :1, " +
                "expected int or java.lang.Integer " +
                "but class java.lang.String");
        Dao dao = mango.create(Dao.class);
        dao.add2("");
    }

    @Test
    public void test3() {
        thrown.expect(IncorrectParameterTypeException.class);
        thrown.expectMessage("batch update expected one and only one parameter but 2");
        Dao dao = mango.create(Dao.class);
        dao.batchAdd(new ArrayList<Integer>(), 1);
    }

    @Test
    public void test4() {
        thrown.expect(IncorrectParameterTypeException.class);
        thrown.expectMessage("parameter of batch update " +
                "expected array or subclass of java.util.Collection " +
                "but class java.lang.Integer");
        Dao dao = mango.create(Dao.class);
        dao.batchAdd2(1);
    }

    @Test
    public void test5() {
        thrown.expect(IncorrectParameterTypeException.class);
        thrown.expectMessage("invalid type of :1, " +
                "expected array or subclass of java.util.Collection " +
                "but int");
        Dao dao = mango.create(Dao.class);
        dao.get(1);
    }

    @Test
    public void test6() {
        thrown.expect(IncorrectParameterTypeException.class);
        thrown.expectMessage("invalid actual type of :1, actual type of :1 " +
                "expected a class can be identified by jdbc " +
                "but java.util.List<java.lang.Integer>");
        Dao dao = mango.create(Dao.class);
        dao.get2(new ArrayList<List<Integer>>());
    }

    @Test
    public void test7() {
        thrown.expect(IncorrectParameterTypeException.class);
        thrown.expectMessage("invalid component type of :1, component type of :1 " +
                "expected a class can be identified by jdbc " +
                "but class java.lang.Object");
        Dao dao = mango.create(Dao.class);
        dao.get3(new Object[]{});
    }

    @Test
    public void test8() {
        thrown.expect(IncorrectParameterTypeException.class);
        thrown.expectMessage("invalid type of :1, " +
                "expected a class can be identified by jdbc " +
                "but class java.lang.Object");
        Dao dao = mango.create(Dao.class);
        dao.get4(new Object());
    }

    @Test
    public void test9() {
        thrown.expect(IncorrectParameterTypeException.class);
        thrown.expectMessage("invalid type of :1.obj, " +
                "expected a class can be identified by jdbc " +
                "but class java.lang.Object");
        Dao9 dao = mango.create(Dao9.class);
        dao.get(new Model());
    }

    @Test
    public void test10() {
        thrown.expect(IncorrectParameterTypeException.class);
        thrown.expectMessage("invalid actual type of :1.objs, actual type of :1.objs " +
                "expected a class can be identified by jdbc " +
                "but class java.lang.Object");
        Dao9 dao = mango.create(Dao9.class);
        dao.get2(new Model());
    }


    @DB
    static interface Dao {
        @SQL("insert into ${1 + :1} ...")
        public int add(Object obj);

        @SQL("insert into ${1 + :1 * 10} ...")
        public int add2(String str);

        @SQL("insert into ...")
        public int[] batchAdd(List<Integer> list, int a);

        @SQL("insert into ...")
        public int[] batchAdd2(Integer a);

        @SQL("select ... where a in (:1)")
        public List<Integer> get(int a);

        @SQL("select ... where a in (:1)")
        public List<Integer> get2(List<List<Integer>> list);

        @SQL("select ... where a in (:1)")
        public List<Integer> get3(Object[] objs);

        @SQL("select ... where a=:1")
        public int get4(Object obj);
    }

    @DB
    @Cache(prefix = "dao9_", expire = Day.class)
    static interface Dao9 {
        @SQL("select ... where a=:1.uid")
        public int get(@CacheBy("obj") Model model);

        @SQL("select ... where a=:1.uid")
        public int get2(@CacheBy("objs") Model model);
    }

    static class Model {
        Object obj = null;
        int uid;
        List<Object> objs;
        public Object getObj() {
            return obj;
        }
        public int getUid() {
            return uid;
        }
        public List<Object> getObjs() {
            return objs;
        }
    }

}
