package cc.concurrent.mango.parser;

import org.junit.Test;

/**
 * @author ash
 */
public class ParserTest {

    @Test
    public void test() throws ParseException {
        Parser p = new Parser("a=:1.abc.ed  sdf");
        p.parse().dump("");
    }

}