package cc.concurrent.mango.runtime.parser;

/**
 * @author ash
 */
public class ASTAddNode extends MathExpression {

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

}