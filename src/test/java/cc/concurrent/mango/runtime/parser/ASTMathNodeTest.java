package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.RuntimeContextImpl;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

/**
 * @author ash
 */
public class ASTMathNodeTest {

    @Test
    public void test() throws ParseException {
        Parser p = new Parser("${:1 % 100 + :2}");
        SimpleNode root = p.parse();
        Map<String, Object> map = Maps.newHashMap();
        map.put("1", 1000);
        map.put("2", null);
        RuntimeContext context = new RuntimeContextImpl(map);
        System.out.println(root.jjtGetChild(0).value(context));
    }

}
