package cc.concurrent.mango.runtime.parser;

import com.google.common.base.Objects;
import com.google.common.base.Splitter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author ash
 */
public class ASTOutParam extends SimpleNode {

    private int num ;
    private List<String> fieldNames;

    public ASTOutParam(int i) {
        super(i);
    }

    public ASTOutParam(Parser p, int i) {
        super(p, i);
    }

    public void setParam(String param) {
        Pattern p = Pattern.compile(":(\\d+)(\\.\\w+)*");
        Matcher m = p.matcher(param);
        checkState(m.matches());
        num = Integer.parseInt(m.group(1));
        String strFieldNames = param.substring(m.end(1));
        fieldNames = Splitter.on(".").omitEmptyStrings().splitToList(strFieldNames);
    }

    @Override
    public String toString() {
        StringBuffer sb = new StringBuffer();
        sb.append(num);
        for (String fieldName : fieldNames) {
            sb.append(".").append(fieldName);
        }
        return Objects.toStringHelper(this).addValue(sb).toString();
    }
}
