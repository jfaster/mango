package cc.concurrent.mango.parser;

import com.google.common.base.Objects;

/**
 * @author ash
 */
public class ASTAddNode extends SimpleNode {

    public ASTAddNode(int i) {
        super(i);
    }

    public ASTAddNode(Parser p, int i) {
        super(p, i);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue("+").toString();
    }
}