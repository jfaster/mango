package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.ParsedSql;
import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.RuntimeContextImpl;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

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
    public void test2() throws ParseException {
        Parser p = new Parser("select * from table_${:1 % 100} where a = :1 and b in (:2)");
        ASTRootNode root = p.parse();
        Map<String, Object> map = Maps.newHashMap();
        map.put("1", 105);
        map.put("2", Lists.newArrayList());
        RuntimeContext context = new RuntimeContextImpl(map);
        ParsedSql tuple = root.getSqlAndArgs(context);
        System.out.println(tuple.getSql());
        System.out.println(Arrays.toString(tuple.getArgs()));
    }

    @Test
    public void test3() throws Exception {
        Parser p = new Parser("select id, name, age, gender, money, update_time from user where id in (:1)");
        p.parse().dump("");
    }

}