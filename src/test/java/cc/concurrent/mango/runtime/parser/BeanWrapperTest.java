package cc.concurrent.mango.runtime.parser;

import com.google.common.collect.Maps;
import org.junit.Test;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;

import java.util.Map;

/**
 * @author ash
 */
public class BeanWrapperTest {

    @Test
    public void test() {
        A a = new A(100);
        BeanWrapper bw = new BeanWrapperImpl(a);
        System.out.println(bw.getPropertyValue("i"));
    }

    @Test
    public void test2() {
        Map<String, Object> cache = Maps.newHashMap();
        System.out.println(cache.containsKey("key"));
        cache.put("key", null);
        System.out.println(cache.containsKey("key"));
        System.out.println(cache.get("key"));
    }


    public static class A {
        int i;

        A(int i) {
            this.i = i;
        }

        public int getI() {
            return i;
        }

        public void setI(int i) {
            this.i = i;
        }
    }

}
