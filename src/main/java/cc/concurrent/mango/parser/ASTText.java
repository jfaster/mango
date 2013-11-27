package cc.concurrent.mango.parser;

import com.google.common.base.Objects;

/**
 * @author ash
 */
public class ASTText extends SimpleNode {

    private String text;

    public ASTText(int i) {
        super(i);
    }

    public ASTText(Parser p, int i) {
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
