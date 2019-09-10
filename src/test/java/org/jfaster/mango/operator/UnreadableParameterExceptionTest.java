package org.jfaster.mango.operator;

import org.jfaster.mango.annotation.DB;
import org.jfaster.mango.annotation.SQL;
import org.jfaster.mango.binding.BindingException;
import org.jfaster.mango.support.DataSourceConfig;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

/**
 * @author ash
 */
public class UnreadableParameterExceptionTest {

  private final static Mango mango = Mango.newInstance(DataSourceConfig.getDataSource());

  @Rule
  public ExpectedException thrown = ExpectedException.none();

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

    @SQL("insert into user(uid) values(:1)")
    public int add();

    @SQL("select uid from user where uid in (:1)")
    public int[] gets();
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
