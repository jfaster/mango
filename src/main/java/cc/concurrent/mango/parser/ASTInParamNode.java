package cc.concurrent.mango.parser;

import com.google.common.base.CharMatcher;
import com.google.common.base.Objects;
import com.google.common.base.Splitter;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.google.common.base.Preconditions.checkState;

/**
 * @author ash
 */
public class ASTInParamNode extends SimpleNode {

    private int num ;
    private List<String> fieldNames;

    public ASTInParamNode(int i) {
        super(i);
    }

    public ASTInParamNode(Parser p, int i) {
        super(p, i);
    }

    public void setParam(String param) {
        Pattern p = Pattern.compile("\\(\\s*:(\\d+)(\\.\\w+)*\\s*\\)");
        Matcher m = p.matcher(param);
        checkState(m.matches());
        num = Integer.parseInt(m.group(1));
        String strFieldNames = param.substring(m.end(1));
        System.out.println(strFieldNames);
        fieldNames = Splitter.on(".").trimResults(CharMatcher.is(')').or(CharMatcher.is(' ')))
                .omitEmptyStrings().splitToList(strFieldNames);
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
