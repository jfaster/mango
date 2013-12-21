package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.RuntimeContext;
import com.google.common.base.CharMatcher;
import com.google.common.collect.Lists;

import java.lang.reflect.Array;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.*;

/**
 * @author ash
 */
public class ASTInParam extends SimpleNode {

    private String beanName;
    private String propertyName; // 为""的时候表示没有属性

    public ASTInParam(int i) {
        super(i);
    }

    public ASTInParam(Parser p, int i) {
        super(p, i);
    }

    public void setParam(String param) {
        Pattern p = Pattern.compile("in\\s*\\(\\s*:(\\w+)(\\.\\w+)*\\s*\\)");
        Matcher m = p.matcher(param);
        checkState(m.matches());
        beanName = m.group(1);
        CharMatcher toRemove = CharMatcher.is(' ').or(CharMatcher.is(')'));
        propertyName = toRemove.removeFrom(param.substring(m.end(1))); // 去掉空格与)
        if (!propertyName.isEmpty()) {
            propertyName = propertyName.substring(1);  // .a.b.c变为a.b.c
        }
    }

    public List<Object> values(RuntimeContext context) {
        Object v = context.getPropertyValue(beanName, propertyName);
        checkNotNull(v, "parameter " + getParamName() + " can't be null");
        List<Object> values = Lists.newArrayList();
        if (v.getClass().isArray()) { // 数组
            int size = Array.getLength(v);
            for (int i = 0; i < size; i++) {
                values.add(Array.get(v, i));
            }
        } else if (v instanceof Collection) { // 集合
            for (Object o : (Collection) v) {
                values.add(o);
            }
        } else {
            throw new IllegalArgumentException("parameter " + getParamName() + " must be array or instance of Collection");
        }
        checkArgument(!values.isEmpty(), "value of parameter " + getParamName() + " can't be empty");
        return values;
    }

    private String getParamName() {
        return ":" + beanName + (propertyName.isEmpty() ? "" : "." + propertyName);
    }

}
