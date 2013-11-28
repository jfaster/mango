package cc.concurrent.mango.parser;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author ash
 */
public class ASTParam extends SimpleNode {

    private int num;
    private String propertyName;

    public ASTParam(int i) {
        super(i);
    }

    public ASTParam(Parser p, int i) {
        super(p, i);
    }

    public void setParam(String param) {
        Pattern p = Pattern.compile(":(\\d+)(\\.\\w+)*");
        Matcher m = p.matcher(param);
        checkState(m.matches());
        num = Integer.parseInt(m.group(1));
        propertyName = param.substring(m.end(1));
        if (!propertyName.isEmpty()) {
            propertyName = propertyName.substring(1);  // .a.b.c变为a.b.c
        }
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(num + "." + propertyName).toString();
    }
}