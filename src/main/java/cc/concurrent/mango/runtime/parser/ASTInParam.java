package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.RuntimeContext;
import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;

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
        Pattern p = Pattern.compile("\\(\\s*:(\\w+)(\\.\\w+)*\\s*\\)");
        Matcher m = p.matcher(param);
        checkState(m.matches());
        beanName = m.group(1);
        CharMatcher toRemove = CharMatcher.is(' ').or(CharMatcher.is(')'));
        propertyName = toRemove.removeFrom(param.substring(m.end(1))); // 去掉空格与)
        if (!propertyName.isEmpty()) {
            propertyName = propertyName.substring(1);  // .a.b.c变为a.b.c
        }
    }

    @Override
    public Object value(RuntimeContext context) {
        return context.getPropertyValue(beanName, propertyName);
    }

    @Override
    public String toString() {
        String str = propertyName.isEmpty() ? "" : "." + propertyName;
        return Objects.toStringHelper(this).addValue(beanName + str).toString();
    }

}
