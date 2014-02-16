package cc.concurrent.mango.runtime.parser;

/**
 * @author ash
 */
public class ASTMulExpression extends MathExpression {

    public ASTMulExpression(int i) {
        super(i);
    }

    public ASTMulExpression(Parser p, int i) {
        super(p, i);
    }

    @Override
    public Integer perform(Integer left, Integer right) {
        return left * right;
    }

}