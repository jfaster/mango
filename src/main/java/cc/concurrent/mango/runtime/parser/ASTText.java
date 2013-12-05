package cc.concurrent.mango.runtime.parser;

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

}
