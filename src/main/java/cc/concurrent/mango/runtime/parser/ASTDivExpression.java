package cc.concurrent.mango.runtime.parser;

/**
 * @author ash
 */
public class ASTDivExpression extends MathExpression {

    public ASTDivExpression(int i) {
        super(i);
    }

    public ASTDivExpression(Parser p, int i) {
        super(p, i);
    }

    @Override
    public Integer perform(Integer left, Integer right) {
        return left / right;
    }

}