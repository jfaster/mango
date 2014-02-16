package cc.concurrent.mango.runtime.parser;

/**
 * @author ash
 */
public class ASTSubtractExpression extends MathExpression {

    public ASTSubtractExpression(int i) {
        super(i);
    }

    public ASTSubtractExpression(Parser p, int i) {
        super(p, i);
    }

    @Override
    public Integer perform(Integer left, Integer right) {
        return left - right;
    }

}
