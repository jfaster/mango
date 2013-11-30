package cc.concurrent.mango.runtime.parser;

import com.google.common.base.Objects;

/**
 * @author ash
 */
public class ASTDivNode extends SimpleNode {

    public ASTDivNode(int i) {
        super(i);
    }

    public ASTDivNode(Parser p, int i) {
        super(p, i);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue("/").toString();
    }
}