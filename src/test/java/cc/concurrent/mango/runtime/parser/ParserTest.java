package cc.concurrent.mango.runtime.parser;

import org.junit.Test;

/**
 * @author ash
 */
public class ParserTest {

    @Test
    public void test() throws Exception {
        Parser p = new Parser("select * from ${:table / 100 + 1} where a=:1");
        p.parse().dump("");
    }

    @Test
    public void test2() throws Exception {
        Parser p = new Parser("${(:2  + :1) + 3}");
        p.parse().dump("");
    }

    @Test
    public void test3() throws Exception {
        Parser p = new Parser("${((:2  + :1)) + 3}");
        p.parse().dump("");
    }

}