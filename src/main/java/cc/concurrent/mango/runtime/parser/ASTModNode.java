package cc.concurrent.mango.runtime.parser;

/**
 * @author ash
 */
public class ASTModNode extends MathExpression {

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

}