package cc.concurrent.mango.util.reflect;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @author ash
 */
public class TypeUtilTest {

    @Test
    public void testGetPropertyType() throws Exception {
        assertThat(TypeUtil.getPropertyType(A.class, "b.i", "1").equals(Integer.class), equalTo(true));
        assertThat(TypeUtil.getPropertyType(A.class, "s", "2").equals(String.class), equalTo(true));
        TypeUtil.getPropertyType(A.class, "b.j", "3");
    }


    public static class A {

        private B b;
        private String s;

        public B getB() {
            return b;
        }

        public void setB(B b) {
            this.b = b;
        }

        public String getS() {
            return s;
        }

        public void setS(String s) {
            this.s = s;
        }
    }

    public static class B {
        private Integer i;

        public Integer getI() {
            return i;
        }

        public void setI(Integer i) {
            this.i = i;
        }
    }

}
