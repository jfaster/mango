package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;

/**
 * @author ash
 */
public abstract class ASTExpressionNode extends SimpleNode {

    public ASTExpressionNode(int i) {
        super(i);
    }

    public ASTExpressionNode(Parser p, int i) {
        super(p, i);
    }

    public abstract Object value(RuntimeContext context);

    public abstract void checkType(TypeContext context);

}
