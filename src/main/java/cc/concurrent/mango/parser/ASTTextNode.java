package cc.concurrent.mango.parser;

import com.google.common.base.Objects;

/**
 * @author ash
 */
public class ASTTextNode extends SimpleNode {

    private String text;

    public ASTTextNode(int i) {
        super(i);
    }

    public ASTTextNode(Parser p, int i) {
        super(p, i);
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue(text).toString();
    }
}
