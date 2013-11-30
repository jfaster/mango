package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.RuntimeContextImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import org.junit.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author ash
 */
public class ASTInParamTest {

    @Test
    public void test() throws ParseException {
        Parser p = new Parser("(:1)");
        SimpleNode root = p.parse();
        Map<String, Object> map = Maps.newHashMap();
        String[] strs = new String[] {"a", "b"};
        map.put("1", strs);
        RuntimeContext context = new RuntimeContextImpl(map);
        System.out.println(((ASTInParam) root.jjtGetChild(0)).values(context));
    }

    @Test
    public void test2() throws ParseException {
        Parser p = new Parser("(:1)");
        SimpleNode root = p.parse();
        Map<String, Object> map = Maps.newHashMap();
        Set<String> strs = Sets.newHashSet("abc", "def");
        map.put("1", strs);
        RuntimeContext context = new RuntimeContextImpl(map);
        System.out.println(((ASTInParam) root.jjtGetChild(0)).values(context));
    }

}
