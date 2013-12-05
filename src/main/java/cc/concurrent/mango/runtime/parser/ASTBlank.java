package cc.concurrent.mango.runtime.parser;

/**
 * @author ash
 */
public class ASTBlank extends SimpleNode {

    private String blank;

    public ASTBlank(int i) {
        super(i);
    }

    public ASTBlank(Parser p, int i) {
        super(p, i);
    }

    public String getBlank() {
        return blank;
    }

    public void setBlank(String blank) {
        this.blank = blank;
    }

}
