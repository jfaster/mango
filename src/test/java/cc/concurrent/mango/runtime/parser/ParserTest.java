package cc.concurrent.mango.runtime.parser;

import org.junit.Test;

/**
 * @author ash
 */
public class ParserTest {

    @Test
    public void test() throws Exception {
        Parser p = new Parser("select * from ${:table / 100 + :1} where a=:2 and b in (:3)");
        p.parse().dump("");
    }

    @Test
    public void test2() throws Exception {
        Parser p = new Parser("insert into table(id, name) values(:1, :2)");
        p.parse().dump("");
    }

}