package org.jfaster.mango.operator;

import org.jfaster.mango.annotation.Cache;
import org.jfaster.mango.annotation.CacheBy;
import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.binding.BindingException;
import org.jfaster.mango.operator.cache.Day;
import org.jfaster.mango.operator.cache.LocalCacheHandler;
import org.jfaster.mango.support.DataSourceConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class UnreadableParameterExceptionTest {

  private final static Mango mango = Mango.newInstance(DataSourceConfig.getDataSource());

  static {
    mango.setLazyInit(true);
    mango.setCacheHandler(new LocalCacheHandler());
  }

  @Rule
  public ExpectedException thrown = ExpectedException.none();

  @Test
  public void test2() {
    thrown.expect(BindingException.class);
    thrown.expectMessage("Parameter ':1.c.d' can't be readable; caused by: There is no getter/setter for property named 'c' in 'class org.jfaster.mango.operator.UnreadableParameterExceptionTest$A'");
    Dao dao = mango.create(Dao.class);
    dao.add2(new A());
  }

  @Test
  public void test3() {
    thrown.expect(BindingException.class);
    thrown.expectMessage("if use cache and sql has one in clause, property c of " +
        "class org.jfaster.mango.operator.UnreadableParameterExceptionTest$A " +
        "expected readable but not");
    Dao2 dao = mango.create(Dao2.class);
    dao.gets(new ArrayList<Integer>());
  }

  @Test
  public void test4() {
    thrown.expect(BindingException.class);
    thrown.expectMessage("Parameter ':1' not found, available root parameters are []");
    Dao dao = mango.create(Dao.class);
    dao.add();
  }

  @Test
  public void test5() {
    thrown.expect(BindingException.class);
    thrown.expectMessage("Parameter ':1' not found, available root parameters are []");
    Dao dao = mango.create(Dao.class);
    dao.gets();
  }

  @DB
  static interface Dao {
    @SQL("insert into user(uid) values (:1.b.d)")
    public int add(A a);

    @SQL("insert into user(uid) values (:1.c.d)")
    public int add2(A a);

    @SQL("insert into user(uid) values(:1)")
    public int add();

    @SQL("select uid from user where uid in (:1)")
    public int[] gets();
  }

  @DB
  @Cache(prefix = "dao2_", expire = Day.class)
  static interface Dao2 {
    @SQL("select ... where c in (:1)")
    public List<A> gets(@CacheBy List<Integer> ids);
  }

  static class A {
    B b;

    public B getB() {
      return b;
    }
  }

  static class B {
    C c;

    public C getC() {
      return c;
    }
  }

  static class C {

  }

}
