package cc.concurrent.mango.parser;

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
}
