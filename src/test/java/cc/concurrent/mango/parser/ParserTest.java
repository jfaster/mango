package cc.concurrent.mango.parser;

import org.junit.Test;

/**
 * @author ash
 */
public class ParserTest {

    @Test
    public void test() throws ParseException {
        Parser p = new Parser("select * from table_${:1 % 100 + 1 + :3.uid} where a = :1 and b in (:2)");
        p.parse().dump("");
    }

}