package cc.concurrent.mango.runtime.parser;

import java.util.ArrayList;
import java.util.List;

/**
 * @author ash
 */
public class ASTRootNode extends SimpleNode {

    List<Token> tokens = new ArrayList<Token>();

    public ASTRootNode(int i) {
        super(i);
    }

    public ASTRootNode(Parser p, int i) {
        super(p, i);
    }

    public void add(Token t) {
        tokens.add(t);
    }

    @Override
    public String toString() {
        return tokens.toString();
    }
}
