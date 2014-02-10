package cc.concurrent.mango.runtime.parser;

/**
 * @author ash
 */
public class ASTDivNode extends MathExpression {

    public ASTDivNode(int i) {
        super(i);
    }

    public ASTDivNode(Parser p, int i) {
        super(p, i);
    }

    @Override
    public Integer perform(Integer left, Integer right) {
        return left / right;
    }

}