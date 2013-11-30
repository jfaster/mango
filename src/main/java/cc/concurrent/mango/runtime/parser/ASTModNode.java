package cc.concurrent.mango.runtime.parser;

import com.google.common.base.Objects;

/**
 * @author ash
 */
public class ASTModNode extends ASTMathNode {

    public ASTModNode(int i) {
        super(i);
    }

    public ASTModNode(Parser p, int i) {
        super(p, i);
    }

    @Override
    public Integer perform(Integer left, Integer right) {
        return left % right;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).addValue("%").toString();
    }
}