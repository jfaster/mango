package cc.concurrent.mango.runtime.parser;

/**
 * @author ash
 */
public class ASTSubtractNode extends ASTMathNode {

    public ASTSubtractNode(int i) {
        super(i);
    }

    public ASTSubtractNode(Parser p, int i) {
        super(p, i);
    }

    @Override
    public Integer perform(Integer left, Integer right) {
        return left - right;
    }

}
