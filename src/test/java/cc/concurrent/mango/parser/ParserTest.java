package cc.concurrent.mango.parser;

import org.junit.Test;

/**
 * @author ash
 */
public class ParserTest {

    @Test
    public void test() throws ParseException {
        Parser p = new Parser("sdfs df :1 s df");
        p.parse().dump("--");
    }

}