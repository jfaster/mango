package cc.concurrent.mango.runtime.parser;

import com.google.common.base.Objects;

/**
 * @author ash
 */
public class ASTModNode extends SimpleNode {

    public ASTModNode(int i) {
        super(i);
    }

    public ASTModNode(Parser p, int i) {
        super(p, i);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue("%").toString();
    }
}