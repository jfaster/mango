package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.TypeContext;
import cc.concurrent.mango.runtime.TypeContextImpl;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Map;

/**
 * @author ash
 */
public class ParserTypeTest {

    @Test
    public void test() throws Exception {
        Parser p = new Parser("${:1  * (:2 + 3)}");
        ASTRootNode root = p.parse();
        Map<String, Class<?>> map = Maps.newHashMap();
        map.put("1", Integer.class);
        map.put("2", String.class);
        TypeContext context = new TypeContextImpl(map);
        root.checkType(context);
    }

    @Test
    public void test2() throws Exception {
        Parser p = new Parser("${:1  + (:2 + 3)}");
        ASTRootNode root = p.parse();
        Map<String, Class<?>> map = Maps.newHashMap();
        map.put("1", Integer.class);
        map.put("2", String.class);
        TypeContext context = new TypeContextImpl(map);
        root.checkType(context);
    }

    @Test
    public void test3() throws Exception {
        Parser p = new Parser("a in (:1)");
        ASTRootNode root = p.parse();
        Map<String, Class<?>> map = Maps.newHashMap();
        map.put("1", Integer.class);
        TypeContext context = new TypeContextImpl(map);
        root.checkType(context);
    }




}
