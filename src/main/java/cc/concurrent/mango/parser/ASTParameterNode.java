package cc.concurrent.mango.parser;

import com.google.common.collect.Lists;

import java.util.List;

/**
 * @author ash
 */
public class ASTParameterNode extends SimpleNode {

    private int num ;
    private List<String> fieldNames = Lists.newArrayList();

    public ASTParameterNode(int i) {
        super(i);
    }

    public ASTParameterNode(Parser p, int i) {
        super(p, i);
    }

    public void setNum(String strNum) {
        this.num = Integer.parseInt(strNum);
    }

    public void addFieldName(String fieldName) {
        fieldNames.add(fieldName);
    }

}
