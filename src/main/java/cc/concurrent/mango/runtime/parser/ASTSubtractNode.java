package cc.concurrent.mango.runtime.parser;

import com.google.common.base.Objects;

/**
 * @author ash
 */
public class ASTSubtractNode extends SimpleNode {

    public ASTSubtractNode(int i) {
        super(i);
    }

    public ASTSubtractNode(Parser p, int i) {
        super(p, i);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue("-").toString();
    }
}
