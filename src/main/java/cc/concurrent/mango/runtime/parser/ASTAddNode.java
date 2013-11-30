package cc.concurrent.mango.runtime.parser;

import com.google.common.base.Objects;

/**
 * @author ash
 */
public class ASTAddNode extends ASTMathNode {

    public ASTAddNode(int i) {
        super(i);
    }

    public ASTAddNode(Parser p, int i) {
        super(p, i);
    }

    @Override
    protected Object handleSpecial(Object left, Object right) {
        if (left == null) {
            left = "null";
        } else if (right == null) {
            right = "null";
        }
        if (left instanceof String || right instanceof String) {
            return left.toString().concat(right.toString());
        }
        return null;
    }

    @Override
    public Integer perform(Integer left, Integer right) {
        return left + right;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue("+").toString();
    }
}