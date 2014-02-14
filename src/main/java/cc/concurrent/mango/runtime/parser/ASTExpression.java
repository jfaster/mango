package cc.concurrent.mango.runtime.parser;

import cc.concurrent.mango.runtime.RuntimeContext;
import cc.concurrent.mango.runtime.TypeContext;

/**
 * @author ash
 */
public class ASTExpression extends ValuableExpression {

    public ASTExpression(int i) {
        super(i);
    }

    public ASTExpression(Parser p, int i) {
        super(p, i);
    }

    @Override
    Object value(RuntimeContext context) {
        return ((ValuableExpression) jjtGetChild(0)).value(context);
    }

    @Override
    void checkType(TypeContext context) {
        ((ValuableExpression) jjtGetChild(0)).checkType(context);
    }

    @Override
    Token getFirstToken() {
        return jjtGetFirstToken();
    }

    @Override
    Token getLastToken() {
        return jjtGetLastToken();
    }

}
