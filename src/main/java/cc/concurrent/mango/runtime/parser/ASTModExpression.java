package cc.concurrent.mango.runtime.parser;

/**
 * @author ash
 */
public class ASTModExpression extends MathExpression {

    public ASTModExpression(int i) {
        super(i);
    }

    public ASTModExpression(Parser p, int i) {
        super(p, i);
    }

    @Override
    public Integer perform(Integer left, Integer right) {
        return left % right;
    }

}