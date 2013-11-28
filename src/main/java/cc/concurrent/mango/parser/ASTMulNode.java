package cc.concurrent.mango.parser;

import com.google.common.base.Objects;

/**
 * @author ash
 */
public class ASTMulNode extends SimpleNode {

    public ASTMulNode(int i) {
        super(i);
    }

    public ASTMulNode(Parser p, int i) {
        super(p, i);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue("*").toString();
    }
}