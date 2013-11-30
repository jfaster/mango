package cc.concurrent.mango.runtime.parser;

import org.junit.Test;

/**
 * @author ash
 */
public class ParserTest {

    @Test
    public void test() throws ParseException {
        Parser p = new Parser("select * from table_${:uid % 100 + 1 + :3.uid} where a = :1 and b in (:2.a)");
        p.parse().dump("");
    }

    @Test
    public void test2() {
        System.out.println(this.getClass().isArray());
    }


}