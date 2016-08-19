package org.jfaster.mango.invoker;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class MetaObjectTest {

    @Test
    public void testSetValue() throws Exception {
        A a = new A();
        MetaObject m = MetaObject.forObject(a);
        m.setValue("b.i", 1);
        m.setValue("b.j", "ash");
        m.setValue("b.k", null);
        m.setValue("c", null);
        m.setValue("d", 2);

        assertThat(a.getB().getI(), equalTo(1));
        assertThat(a.getB().getJ(), equalTo("ash"));
        assertThat(a.getB().getK(), nullValue());
        assertThat(a.getC(), nullValue());
        assertThat(a.getD(), equalTo(2));
    }

    public static class A {

        private B b;
        private Integer c;
        private int d;

        public B getB() {
            return b;
        }

        public void setB(B b) {
            this.b = b;
        }

        public Integer getC() {
            return c;
        }

        public void setC(Integer c) {
            this.c = c;
        }

        public int getD() {
            return d;
        }

        public void setD(int d) {
            this.d = d;
        }
    }

    public static class B {

        private int i;
        private String j;
        private Integer k;

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }

        public String getJ() {
            return j;
        }

        public void setJ(String j) {
            this.j = j;
        }

        public Integer getK() {
            return k;
        }

        public void setK(Integer k) {
            this.k = k;
        }
    }


}
