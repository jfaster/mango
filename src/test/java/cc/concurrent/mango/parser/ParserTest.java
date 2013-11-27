package cc.concurrent.mango.parser;

import org.junit.Test;

/**
 * @author ash
 */
public class ParserTest {

    @Test
    public void test() throws ParseException {
        Parser p = new Parser("a=:1.abc.ed :2 ab${:1}cd sdf in ( :3.x.y.x  )");
        p.parse().dump("");
    }

}